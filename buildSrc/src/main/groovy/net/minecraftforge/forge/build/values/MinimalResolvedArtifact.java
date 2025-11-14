package net.minecraftforge.forge.build.values;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.Objects;

public record MinimalResolvedArtifact(MavenInfo info, File file) {
    public static Provider<MinimalResolvedArtifact> from(Project project, TaskProvider<? extends AbstractArchiveTask> task) {
        return project.getProviders().zip(MavenInfo.from(project, task), task.flatMap(AbstractArchiveTask::getArchiveFile), (info, regularFile) ->
            new MinimalResolvedArtifact(info, regularFile.getAsFile())
        );
    }

    public static Provider<MinimalResolvedArtifact> from(Project project, ResolvedArtifactResult artifact) {
        return MavenInfo.from(project, artifact).map(info ->
            new MinimalResolvedArtifact(info, artifact.getFile())
        );
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof MinimalResolvedArtifact that
            && Objects.equals(this.info.name(), that.info.name());
    }
}
