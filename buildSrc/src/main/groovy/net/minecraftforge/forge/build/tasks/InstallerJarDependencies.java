package net.minecraftforge.forge.build.tasks;

import net.minecraftforge.forge.build.values.LibraryInfo;
import net.minecraftforge.forge.build.values.MinimalResolvedArtifact;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.internal.impldep.com.google.common.base.Objects;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class InstallerJarDependencies extends Zip {
    public abstract @Internal Property<Boolean> getOffline();

    protected abstract @Inject ProjectLayout getLayout();

    @Inject
    public InstallerJarDependencies() {
        getOffline().convention(false);

        getArchiveClassifier().set("installer-dependencies");
        getArchiveExtension().set("zip");
        getDestinationDirectory().set(getLayout().getBuildDirectory().dir(getName()));

        //getSlimArtifacts().add(MinimalResolvedArtifact.from(getProject(), getProject().getTasks().named("serverShimJar", Jar.class)));
    }

    @SafeVarargs
    public final void builtFrom(TaskProvider<? extends AbstractArchiveTask>... tasks) {
        for (var task : tasks) {
            builtFrom(task);
        }
    }

    public void builtFrom(TaskProvider<? extends AbstractArchiveTask> task) {
        this.getInputs().file(task);
        this.dependsOn(task);

        var artifact = MinimalResolvedArtifact.from(getProject(), task).get();
        var name = artifact.info().name();
        getLogger().lifecycle("Adding: " + artifact.info().art().name() + ' ' + name);
        this.from(artifact.file(), copy -> copy
            .rename(it -> "maven/" + artifact.info().path())
        );
    }

    public void dependenciesFrom(Configuration configuration) {
        this.getInputs().files(configuration);
        this.dependsOn(configuration.getBuildDependencies());

        var artifacts = MinimalResolvedArtifact.from(getProject(), configuration).get();
        for (var artifact : artifacts) {
            var info = LibraryInfo.from(List.of(artifact)).values().iterator().next().downloads().artifact();
            var pack = getOffline().getOrElse(false) || info.url().isEmpty();

            if (!pack) {
                try {
                    var remote = ResourceGroovyMethods.getText(new URL(info.url() + ".sha1"), "UTF-8");
                    pack = !Objects.equal(info.sha1(), remote);
                } catch (IOException e) {
                    pack = !info.url().startsWith("https://libraries.minecraft.net/");
                }
            }

            if (pack) {
                getLogger().lifecycle("Adding: " + artifact.file().getAbsolutePath());
                this.from(artifact.file(), copy -> copy
                    .rename(it -> "maven/" + info.path())
                );
            }
        }
    }
}
