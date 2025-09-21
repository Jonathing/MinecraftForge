package net.minecraftforge.forge.build;

import net.minecraftforge.forge.build.tasks.Util;
import net.minecraftforge.forge.build.tasks.WriteManifest;
import net.minecraftforge.gradleutils.shared.Closures;
import net.minecraftforge.gradleutils.shared.SharedUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.tasks.Jar;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class BuildLogicPlugin implements Plugin<Object> {
    static {
        Util.init();
    }

    protected abstract @Inject ProviderFactory getProviders();

    protected abstract @Inject ProjectLayout getLayout();

    @Inject
    public BuildLogicPlugin() { }

    @Override
    public void apply(Object target) {
        if (target instanceof Project project)
            apply(project);
    }

    private void apply(Project project) {
        var tasks = project.getTasks();

        project.setGroup("net.minecraftforge");
        project.setVersion(this.getProviders().provider(() -> project.getRootProject().getVersion()));

        SharedUtil.runFirst(project, tasks.register("generateResources"));

        project.getPluginManager().withPlugin("java", javaPlugin -> {

            tasks.withType(Javadoc.class).configureEach(task ->
                task.options(Closures.<CoreJavadocOptions>consumer(options -> {
                    options.setMemberLevel(JavadocMemberLevel.PUBLIC);
                    options.addBooleanOption("Xdoclint:all", true);
                    options.addBooleanOption("-Xdoclint:missing", true);
                }))
            );

            tasks.withType(JavaCompile.class).configureEach(task -> {
                // Needed because we merge the output of this with the output of the compile task. And gradle detects downstream tasks using the output without a hard dep
                task.dependsOn(tasks.named("generateResources"), tasks.named("processResources"));

                var options = task.getOptions();
                options.setWarnings(false); // Shutup deprecated for removal warnings
                options.forkOptions(forkOptions -> {
                    var jvmArgs = new ArrayList<>(Objects.requireNonNullElseGet(forkOptions.getJvmArgs(), List::of));
                    jvmArgs.add("-Xmx6G"); // Needed to make compiling faster, and not run out of heap space in some cases.
                    forkOptions.setJvmArgs(jvmArgs);
                });
            });

            // Merge the resources and classes into the same directory. We'll need to split them at runtime because
            // Minecraft and Forge are in the same sourceSet as they are inter dependent.. for now..
            project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().configureEach(sourceSet -> {
                var dir = getLayout().getBuildDirectory().dir("classes/java/" + sourceSet.getName());
                sourceSet.getOutput().setResourcesDir(dir);
                sourceSet.getJava().getDestinationDirectory().set(dir);
            });
        });

        project.getPluginManager().withPlugin("eclipse", eclipsePlugin -> {
            var eclipse = project.getExtensions().getByType(EclipseModel.class);

            // Run when importing the project
            eclipse.synchronizationTasks(
                tasks.named("eclipseClasspath"),
                tasks.named("eclipseProject")
            );
        });

        project.afterEvaluate(this::finish);
    }

    private void finish(Project project) {
        // We need to write the manifest to the binary file so we have properly versioned packaged at dev time.
        var jar = project.getPluginManager().hasPlugin("net.minecraftforge.forgedev") ? "universalJar" : "jar";
        WriteManifest.register(project, project.getTasks().named(jar, Jar.class));
    }
}
