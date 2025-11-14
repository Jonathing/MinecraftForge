package net.minecraftforge.forge.build.values;

import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.jetbrains.annotations.Nullable;

public record MavenInfo(String key, String name, String path, ArtifactInfo art) {
    public record ArtifactInfo(String group, String name, String version, @Nullable String classifier, String extension) { }

    // NOTE: Relies on Gradle internals
    //       See https://github.com/gradle/gradle/issues/23702
    public static Provider<MavenInfo> from(Project project, ResolvedArtifactResult dependency) {
        String group, name, version, classifier, extension;
        {
            var component = dependency.getId();
            if (component instanceof ModuleComponentArtifactIdentifier moduleId) {
                group = moduleId.getComponentIdentifier().getGroup();
                name = moduleId.getComponentIdentifier().getModule();
                version = moduleId.getComponentIdentifier().getVersion();
                if (moduleId instanceof DefaultModuleComponentArtifactIdentifier defaultId) {
                    classifier = defaultId.getName().getClassifier();
                    extension = defaultId.getName().getExtension();
                } else {
                    classifier = null;
                    extension = null;
                }
            } else if (component.getComponentIdentifier() instanceof ProjectComponentIdentifier projectId) {
                var p = project.findProject(projectId.getProjectPath());
                if (p == null)
                    throw new IllegalArgumentException("Cannot get maven info for a project dependency that has no project: " + projectId);

                group = p.getGroup().toString();
                name = p.getName();
                version = p.getVersion().toString();
                classifier = null;
                extension = null;
            } else {
                throw new IllegalArgumentException("Cannot get maven info for a local/unknown dependency:" + component + " (" + component.getClass() + ')');
            }
        }

        return project.getProviders().of(MavenInfoSource.class, spec -> spec.parameters(parameters -> {
            parameters.getGroup().set(group);
            parameters.getName().set(name);
            parameters.getVersion().set(version);
            parameters.getClassifier().set(classifier);
            parameters.getExtension().set(extension);
        }));
    }

    public static Provider<MavenInfo> from(Project project, TaskProvider<? extends AbstractArchiveTask> task) {
        return project.getProviders().of(MavenInfoSource.class, spec -> spec.parameters(parameters -> {
            parameters.getGroup().set(project.provider(() -> project.getGroup().toString()));
            parameters.getName().set(project.getName());
            parameters.getVersion().set(project.provider(() -> project.getVersion().toString()));
            parameters.getClassifier().set(task.flatMap(AbstractArchiveTask::getArchiveClassifier));
            parameters.getExtension().set(task.flatMap(AbstractArchiveTask::getArchiveExtension));
        }));
    }

    public static Provider<MavenInfo> from(Project project, String classifier) {
        return project.getProviders().of(MavenInfoSource.class, spec -> spec.parameters(parameters -> {
            parameters.getGroup().set(project.provider(() -> project.getGroup().toString()));
            parameters.getName().set(project.getName());
            parameters.getVersion().set(project.provider(() -> project.getVersion().toString()));
            parameters.getClassifier().set(classifier);
        }));
    }
}
