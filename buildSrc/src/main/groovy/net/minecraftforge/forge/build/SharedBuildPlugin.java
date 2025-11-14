package net.minecraftforge.forge.build;

import net.minecraftforge.forge.build.tasks.Util;
import net.minecraftforge.forge.build.tasks.WriteManifest;
import net.minecraftforge.gradleutils.shared.SharedUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath;
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

import javax.inject.Inject;

abstract class SharedBuildPlugin implements Plugin<Project> {
    static {
        Util.init();
    }

    protected abstract @Inject ProjectLayout getLayout();

    @Inject
    public SharedBuildPlugin() { }

    @Override
    public void apply(Project project) {
        project.setGroup("net.minecraftforge");

        var layout = getLayout();

        var tasks = project.getTasks();

        var generateResources = SharedUtil.runFirst(project, tasks.register("generateResources"));
        var processResources = tasks.named("processResources", ProcessResources.class, task ->
            task.dependsOn(generateResources)
        );

        project.getPluginManager().withPlugin("java", javaAppliedPlugin -> {
            tasks.withType(Javadoc.class).configureEach(task -> {
                task.options(minimalOptions -> {
                    if (minimalOptions instanceof CoreJavadocOptions coreOptions) {
                        coreOptions.setMemberLevel(JavadocMemberLevel.PUBLIC);
                        coreOptions.addBooleanOption("Xdoclint:all", true);
                        coreOptions.addBooleanOption("-Xdoclint:missing", true);
                    }
                });
            });

            tasks.withType(JavaCompile.class).configureEach(task -> {
                // Needed because we merge the output of this with the output of the compile task. And gradle detects downstream tasks using the output without a hard dep
                task.dependsOn(generateResources, processResources);
                task.getOptions().setWarnings(false); // Shutup deprecated for removal warnings
                task.getOptions().getForkOptions().setMemoryMaximumSize("6G"); // Needed to make compiling faster, and not run out of heap space in some cases.
            });

            // TODO This is also done by ForgeDev. Consolidate.
            // Merge the resources and classes into the same directory. We'll need to split them at runtime because
            // Minecraft and Forge are in the same sourceSet as they are inter dependent.. for now..
            project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().configureEach(sourceSet -> {
                var dir = layout.getBuildDirectory().dir("sourceSets/" + sourceSet.getName());
                sourceSet.getOutput().setResourcesDir(dir);
                sourceSet.getJava().getDestinationDirectory().set(dir);
            });
        });

        project.getPluginManager().withPlugin("eclipse", eclipseAppliedPlugin -> {
            var eclipse = project.getExtensions().getByType(EclipseModel.class);

            eclipse.synchronizationTasks(
                tasks.named("eclipseClasspath", GenerateEclipseClasspath.class),
                tasks.named("eclipseProject", GenerateEclipseProject.class)
            );
        });

        project.afterEvaluate(this::finish);
    }

    private void finish(Project project) {
        var tasks = project.getTasks();

        var jar = project.getPluginManager().hasPlugin("net.minecraftforge.forgedev") ? "universalJar" : "jar";
        WriteManifest.register(project, tasks.named(jar, Jar.class));
    }
}
