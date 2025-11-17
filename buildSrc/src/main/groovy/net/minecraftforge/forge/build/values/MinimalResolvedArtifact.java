package net.minecraftforge.forge.build.values;

import org.gradle.api.Project;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public record MinimalResolvedArtifact(MavenInfo info, File file) implements Serializable {
    public static Provider<MinimalResolvedArtifact> from(Project project, FileCollection files) {
        var info = MavenInfo.from(project);
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

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof MinimalResolvedArtifact that
            && Objects.equals(this.info.name(), that.info.name());
    }
}
