package net.minecraftforge.forge.build.tasks;

import net.minecraftforge.forge.build.values.LibraryInfo;
import net.minecraftforge.forge.build.values.MinimalResolvedArtifact;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.internal.impldep.com.google.common.base.Objects;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class InstallerJar extends Zip {
    public abstract @Input @Optional Property<Boolean> getFat();
    public abstract @Input @Optional Property<Boolean> getOffline();

    public abstract @Input ListProperty<MinimalResolvedArtifact> getBuiltTasks();
    public abstract @Internal SetProperty<MinimalResolvedArtifact> getAllArtifacts();

    public abstract @Input ListProperty<MinimalResolvedArtifact> getSlimArtifacts();

    protected abstract @Inject ProviderFactory getProviders();
    protected abstract @Inject ProjectLayout getLayout();
    protected abstract @Inject ArchiveOperations getArchiveOperations();

    @Inject
    public InstallerJar() {
        getArchiveClassifier().set("installer");
        getArchiveExtension().set("jar");
        getDestinationDirectory().set(getLayout().getBuildDirectory().dir("libs"));

        var installerJson = getProject().getTasks().named("installerJson", InstallerJson.class);
        var launcherJson = getProject().getTasks().named("launcherJson", LauncherJson.class);

        dependsOn(installerJson, launcherJson);
        from(installerJson, launcherJson);

        from(getLayout().getProjectDirectory().file("src/main/resources/url.png"));

        getSlimArtifacts().add(MinimalResolvedArtifact.from(getProject(), getProject().getTasks().named("serverShimJar", Jar.class)));
    }

    public void using(ProviderConvertible<? extends Dependency> dependency) {
        using(dependency.asProvider());
    }

    public void using(Provider<? extends Dependency> dependency) {
        var configuration = getProject().getConfigurations().detachedConfiguration();
        configuration.getDependencies().addLater(dependency);
        using(configuration);
    }

    public void using(Dependency dependency) {
        using(getProject().getConfigurations().detachedConfiguration(dependency));
    }

    private void using(Configuration configuration) {
        configuration.attributes(a -> a.attribute(Bundling.BUNDLING_ATTRIBUTE, getObjectFactory().named(Bundling.class, Bundling.SHADOWED)));
        from(getProviders().provider(() -> getArchiveOperations().zipTree(configuration.getSingleFile())), copy ->
            copy.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
        );
    }

    public void dependenciesFrom(Configuration configuration) {
        this.dependsOn(configuration.getBuildDependencies());

        var project = getProject();
        var configurations = project.getConfigurations();
        var allArtifacts = getAllArtifacts();

        // Find any artifacts from the 'installer' config
        // This config specifies the runtime files we intend for the interaller to have.
        // And are typically what we would be developing and testing alongside Forge.
        // So we may have local modified versions
        for (var dependency : configuration.getDependencies()) {
            if (dependency instanceof ProjectDependency projectDependency) {
               dependenciesFrom(projectDependency);
            } else {
                var c = configurations.detachedConfiguration(dependency);
                for (var artifact : c.getIncoming().getArtifacts().getResolvedArtifacts().get()) {
                    allArtifacts.add(MinimalResolvedArtifact.from(project, artifact));
                }
            }
        }
    }

    private void dependenciesFrom(ProjectDependency projectDependency) {
        var project = getProject();
        var configurations = project.getConfigurations();
        var allArtifacts = getAllArtifacts();

        var subproject = project.project(projectDependency.getPath());

        var singleFile = configurations.detachedConfiguration(projectDependency);
        singleFile.setTransitive(false);
        allArtifacts.add(MinimalResolvedArtifact.from(subproject, singleFile));

        var transitive = configurations.detachedConfiguration(projectDependency);
        for (var d : transitive.getAllDependencies()) {
            if (d.equals(projectDependency)) {
                continue;
            } else if (d instanceof ProjectDependency nestedProjectDependency) {
                dependenciesFrom(nestedProjectDependency);
            } else {
                var c = configurations.detachedConfiguration(d);
                c.setTransitive(false);
                for (var artifact : c.getIncoming().getArtifacts().getResolvedArtifacts().get()) {
                    allArtifacts.add(MinimalResolvedArtifact.from(project, artifact));
                }
            }
        }
    }

    @Override
    protected void copy() {
        if (getOffline().getOrElse(false)) {
            this.copyOffline();
        } else {
            for (var packed : getSlimArtifacts().get()) {
                this.from(packed.file(), copy ->
                    copy.rename(it -> "maven/" + packed.info().path())
                );
            }
        }

        super.copy();
    }

    private void copyOffline() {
        // First find things we build in this project.
        for (var packed : getBuiltTasks().get()) {
            var name = packed.info().name();
            getLogger().lifecycle("Adding: " + packed.info().art().name() + ' ' + name);
            this.from(packed.file(), copy -> copy
                .rename(it -> "maven/" + packed.info().path())
            );
        }

        for (var resolved : getAllArtifacts().get()) {
            var name = resolved.info().name();
            getLogger().lifecycle("-" + name);
            addFile(resolved.file(), resolved.info().art());
        }
    }

    private void addFile(File file, LibraryInfo.Downloads.ArtifactInfo info) {
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
            getLogger().lifecycle("Adding: " + file.getAbsolutePath());
            this.from(file, copy -> copy
                .rename(it -> "maven/" + info.path())
            );
        }
    }
}
