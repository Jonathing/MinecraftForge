package net.minecraftforge.forge.build.tasks;

import net.minecraftforge.forge.build.values.LibraryInfo;
import net.minecraftforge.forge.build.values.MinimalResolvedArtifact;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class InstallerLibraries extends DefaultTask {
    public abstract @Input SetProperty<MinimalResolvedArtifact> getLauncherLibraries();
    public abstract @Input SetProperty<MinimalResolvedArtifact> getInstallerLibraries();
    protected abstract @Internal MapProperty<String, LibraryInfo> getLauncherLibraryInfo();
    protected abstract @Internal MapProperty<String, LibraryInfo> getInstallerLibraryInfo();

    public abstract @Input SetProperty<MinimalResolvedArtifact> getBuiltTasks();

    public abstract @OutputDirectory DirectoryProperty getOutput();

    protected abstract @Inject FileSystemOperations getFileSystemOperations();

    protected abstract @Inject ArchiveOperations getArchiveOperations();

    @Inject
    public InstallerLibraries() {
        getLauncherLibraryInfo().set(getLauncherLibraries().map(dependencies -> LibraryInfo.from(dependencies, true)));
        getInstallerLibraryInfo().set(getInstallerLibraries().map(dependencies -> LibraryInfo.from(dependencies, true)));
    }

    public void builtFrom(TaskProvider<? extends AbstractArchiveTask> task) {
        this.getInputs().file(task);
        this.dependsOn(task);

        this.getBuiltTasks().add(MinimalResolvedArtifact.from(getProject(), task));
        var artifact = MinimalResolvedArtifact.from(getProject(), task).get();
        var name = artifact.info().name();
        //getLogger().lifecycle("Adding: " + artifact.info().art().name() + ' ' + name);
        this.from(artifact.file(), copy -> copy
            .rename(it -> "maven/" + artifact.info().path())
        );
    }

    @TaskAction
    protected void exec() {
        var artifacts = new HashSet<MinimalResolvedArtifact>();
        var libraries = new HashMap<String, LibraryInfo>();
        populate(artifacts, libraries, getInstallerLibraries().get(), getInstallerLibraryInfo().get());
        populate(artifacts, libraries, getLauncherLibraries().get(), getLauncherLibraryInfo().get());

        var a = this.getLauncherLibraries().get();
        var libraries = LibraryInfo.from(this.getLauncherLibraries().get());

        this.getFileSystemOperations().copy(copy -> copy.into(this.getOutput(), dest -> {
            for (var artifact : this.getBuiltTasks().get()) {
                var name = artifact.info().name();
                getLogger().lifecycle("Adding: " + artifact.info().art().name() + ' ' + name);
                dest.from(artifact.file(), file -> file.rename(it -> "maven/" + artifact.info().path()));
            }
        }));
    }

    private void populate(Set<MinimalResolvedArtifact> artifacts, Map<String, LibraryInfo> libraries, Set<MinimalResolvedArtifact> pendingArtifacts, Map<String, LibraryInfo> pendingLibraries) {
        pendingLibraries.forEach((key, library) -> {
            if (library.downloads() == null || library.downloads().artifact() == null || library.downloads().artifact().url())
        });
    }
}
