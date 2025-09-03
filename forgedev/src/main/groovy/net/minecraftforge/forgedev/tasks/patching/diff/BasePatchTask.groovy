package net.minecraftforge.forgedev.tasks.patching.diff

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

import javax.inject.Inject
import java.nio.file.Files

@DisableCachingByDefault(because = 'Abstract super-class, not to be instantiated directly')
@CompileStatic
@PackageScope abstract class BasePatchTask extends BaseDiffPatchExec {
    FileSystemLocationProperty<? extends FileSystemLocation> getPatches() {
        throw new IllegalStateException('Must be overridden with RegularFileProperty or DirectoryProperty')
    }

    // Patch specific
    @Internal Property<File> getRejects() { this.rejects }
    abstract @Input @Optional Property<String> getArchiveRejects()
    abstract @Input @Optional Property<Float> getFuzz()
    abstract @Input @Optional Property<Integer> getOffset()
    abstract @Input @Optional Property<String> getMode()
    abstract @Input @Optional Property<String> getArchivePatches()

    // Patch shared
    abstract @Input @Optional Property<String> getPrefix()

    void setRejects(Provider<?> rejects) {
        final value = rejects.get()

        if (value instanceof RegularFile)
            this.setRejects(value)
        else if (value instanceof Directory)
            this.setRejects(value)
        else
            this.rejects.set((File) rejects)
    }

    void setRejects(RegularFile rejects) {
        this.rejects.set(this.providerFactory.provider {
            rejects.asFile
        })
    }

    void setRejects(Directory rejects) {
        this.rejects.set(this.providerFactory.provider {
            rejects.asFile.tap {
                Files.createDirectories(it.toPath())
            }
        })
    }

    private final Property<File> rejects

    @Inject
    BasePatchTask() {
        if (this.rejects instanceof DirectoryProperty)
            this.archiveRejects.unset().disallowChanges()
        if (this.patches instanceof DirectoryProperty)
            this.archivePatches.unset().disallowChanges()

        this.rejects = this.objectFactory.property(File)
    }

    @Override
    protected void addArguments() {
        super.addArguments()

        //region Patch specific
        if (this.rejects.present)
            this.args('--reject', this.rejects.get().absolutePath)
        if (this.archiveRejects.present)
            this.args('--archive-rejects', this.archiveRejects.get())
        if (this.fuzz.present)
            this.args('--fuzz', this.fuzz.get())
        if (this.offset.present)
            this.args('--offset', this.offset.get())
        if (this.mode.present)
            this.args('--mode', this.mode.get())
        if (this.archivePatches.present)
            this.args('--archive-patches', this.archivePatches.get())
        //endregion

        //region Patch shared
        if (this.prefix.present)
            this.args('--prefix', this.prefix.get())
        //endregion

        //region Patch Task
        // https://github.com/TheCBProject/DiffPatch/blob/204d393ee23f5cd4298f771c7b9157ee21eb3b62/src/main/java/io/codechicken/diffpatch/cli/DiffPatchCli.java#L191
        // --patch {base} {patches}
        this.args(
            '--patch',
            this.input.locationOnly.map(this.problems.ensureFileLocation()).get().asFile.absolutePath,
            this.patches.locationOnly.map(this.problems.ensureFileLocation()).get().asFile.absolutePath
        )
        //endregion
    }
}
