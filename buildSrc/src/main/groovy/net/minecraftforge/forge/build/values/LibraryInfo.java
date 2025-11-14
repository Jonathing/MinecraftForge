package net.minecraftforge.forge.build.values;

import net.minecraftforge.forge.build.tasks.Util;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public record LibraryInfo(String name, Downloads downloads) {
    public record Downloads(ArtifactInfo artifact) {
        public static final class ArtifactInfo {
            private final String path;
            private String url;
            private final String sha1;
            private final long size;

            public ArtifactInfo(String path, String url, String sha1, long size) {
                this.path = path;
                this.url = url;
                this.sha1 = sha1;
                this.size = size;
            }

            public String path() {
                return path;
            }

            public String url() {
                return url;
            }

            public void validateUrl(boolean offline) {
                if (offline || !url.startsWith("https://libraries.minecraft.net/"))
                    return;

                if (!Util.checkExists(url))
                    url = "https://maven.minecraftforge.net/" + url.substring("https://libraries.minecraft.net/".length());
            }

            public String sha1() {
                return sha1;
            }

            public long size() {
                return size;
            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj) || obj instanceof ArtifactInfo that &&
                    Objects.equals(this.path, that.path) &&
                    Objects.equals(this.url, that.url) &&
                    Objects.equals(this.sha1, that.sha1) &&
                    this.size == that.size;
            }

            @Override
            public int hashCode() {
                int result = Objects.hashCode(path);
                result = 31 * result + Objects.hashCode(url);
                result = 31 * result + Objects.hashCode(sha1);
                result = 31 * result + Long.hashCode(size);
                return result;
            }

            @Override
            public String toString() {
                return "ArtifactInfo[" +
                    "path=" + path + ", " +
                    "url=" + url + ", " +
                    "sha1=" + sha1 + ", " +
                    "size=" + size + ']';
            }
        }
    }

    public LibraryInfo(String name, String path, String url, String sha1, long size) {
        this(name, new Downloads(new Downloads.ArtifactInfo(path, url, sha1, size)));
    }

    public LibraryInfo(MavenInfo info, File file, String url) {
        this(info.name(), info.path(), url, Util.sha1(file), file.length());
    }

    public void validateUrl(boolean offline) {
        this.downloads.artifact.validateUrl(offline);
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
            for (var artifact : configuration.getIncoming().getArtifacts()) {
                parameters.getDependencies().add(MinimalResolvedArtifact.from(project, artifact));
            }
        }));
    }
}
