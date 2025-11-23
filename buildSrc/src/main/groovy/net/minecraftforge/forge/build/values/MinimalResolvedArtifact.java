package net.minecraftforge.forge.build.values;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record MinimalResolvedArtifact(MavenInfo info, File file) implements Serializable {
    private static Provider<MinimalResolvedArtifact> from(Project project, ProjectDependency projectDependency, FileCollection files) {
        var info = MavenInfo.from(projectDependency);
        var ret = project.getObjects().property(MinimalResolvedArtifact.class).value(project.provider(files::getSingleFile).map(file ->
            new MinimalResolvedArtifact(info, file)
        ));

        ret.disallowChanges();
        ret.finalizeValueOnRead();
        if (project.getState().getExecuted()) {
            ret.finalizeValue();
        }

        return ret;
    }

    public static Provider<MinimalResolvedArtifact> from(Project project, TaskProvider<? extends AbstractArchiveTask> task) {
        var ret = project.getObjects().property(MinimalResolvedArtifact.class).value(project.getProviders().zip(MavenInfo.from(project, task), task.flatMap(AbstractArchiveTask::getArchiveFile), (info, regularFile) ->
            new MinimalResolvedArtifact(info, regularFile.getAsFile())
        ));

        ret.disallowChanges();
        ret.finalizeValueOnRead();
        if (project.getState().getExecuted()) {
            ret.finalizeValue();
        }

        return ret;
    }

    public static MinimalResolvedArtifact from(Project project, ResolvedArtifactResult artifact) {
        var info = MavenInfo.from(project, artifact);
        return new MinimalResolvedArtifact(info, artifact.getFile());
    }

    public static Provider<List<MinimalResolvedArtifact>> from(Project project, Configuration configuration) {
        var ret = project.getObjects().listProperty(MinimalResolvedArtifact.class);

        var configurations = project.getConfigurations();

        // Find any artifacts from the 'installer' config
        // This config specifies the runtime files we intend for the interaller to have.
        // And are typically what we would be developing and testing alongside Forge.
        // So we may have local modified versions
        for (var dependency : configuration.getDependencies()) {
            if (dependency instanceof ProjectDependency projectDependency) {
                from(project, projectDependency, ret);
            } else {
                var c = configurations.detachedConfiguration(dependency);
                ret.addAll(c.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    var artifacts = new ArrayList<MinimalResolvedArtifact>(results.size());
                    for (var artifact : results) {
                        artifacts.add(MinimalResolvedArtifact.from(null, artifact));
                    }
                    return artifacts;
                }));
            }
        }

        ret.disallowChanges();
        ret.finalizeValueOnRead();
        if (project.getState().getExecuted()) {
            ret.finalizeValue();
        }

        return ret;
    }

    private static void from(Project project, ProjectDependency projectDependency, ListProperty<MinimalResolvedArtifact> ret) {
        var configurations = project.getConfigurations();

        var subproject = project.project(projectDependency.getPath());

        var singleFile = configurations.detachedConfiguration(projectDependency);
        singleFile.setTransitive(false);
        ret.add(MinimalResolvedArtifact.from(subproject, projectDependency, singleFile));

        var transitive = configurations.detachedConfiguration(projectDependency);
        for (var d : transitive.getAllDependencies()) {
            if (d.equals(projectDependency)) {
                continue;
            } else if (d instanceof ProjectDependency nestedProjectDependency) {
                from(project, nestedProjectDependency, ret);
            } else {
                var c = configurations.detachedConfiguration(d);
                c.setTransitive(false);
                for (var artifact : c.getIncoming().getArtifacts().getResolvedArtifacts().get()) {
                    ret.add(MinimalResolvedArtifact.from(project, artifact));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof MinimalResolvedArtifact that
            && Objects.equals(this.info.name(), that.info.name());
    }
}
