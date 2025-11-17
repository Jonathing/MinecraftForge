package net.minecraftforge.forge.build.values;

import net.minecraftforge.forge.build.tasks.Util;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public record LibraryInfo(String name, Downloads downloads) implements Serializable {
    public record Downloads(ArtifactInfo artifact) implements Serializable {
        public record ArtifactInfo(String path, String url, String sha1, long size) implements Serializable {
            public ArtifactInfo validateUrl(boolean offline) {
                if (offline || !url.startsWith("https://libraries.minecraft.net/"))
                    return this;

                if (!Util.checkExists(url))
                    return new ArtifactInfo(path, "https://maven.minecraftforge.net/" + url.substring("https://libraries.minecraft.net/".length()), sha1, size);

                return this;
            }
        }
    }

    public LibraryInfo(String name, String path, String url, String sha1, long size) {
        this(name, new Downloads(new Downloads.ArtifactInfo(path, url, sha1, size)));
    }

    public LibraryInfo(MavenInfo info, File file, String url) {
        this(info.name(), info.path(), url, Util.sha1(file), file.length());
    }

    public LibraryInfo validateUrl(boolean offline) {
        var artifact = this.downloads.artifact.validateUrl(offline);
        if (this.downloads.artifact == artifact)
            return this;

        return new LibraryInfo(name, new Downloads(artifact));
    }

    @SafeVarargs
    public static Provider<Map<String, LibraryInfo>> from(Project project, TaskProvider<? extends AbstractArchiveTask>... tasks) {
        return project.getProviders().of(LibraryInfoSource.class, spec -> spec.parameters(parameters -> {
            for (var task : tasks) {
                parameters.getDependencies().add(MinimalResolvedArtifact.from(project, task));
            }
        }));
    }

    public static Provider<Map<String, LibraryInfo>> from(Project project, Configuration configuration) {
        return project.getProviders().of(LibraryInfoSource.class, spec -> spec.parameters(parameters -> {
            parameters.getDependencies().addAll(configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(artifacts -> {
                var ret = new HashSet<MinimalResolvedArtifact>(artifacts.size());
                for (var artifact : artifacts) {
                    ret.add(MinimalResolvedArtifact.from(project, artifact));
                }
                return ret;
            }));
        }));
    }
}
