package net.minecraftforge.forgedev.patching.diff

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class BaseDiffTask extends BaseDiffPatchExec {
    @PathSensitive(PathSensitivity.ABSOLUTE) FileSystemLocationProperty<? extends FileSystemLocation> getModified() {
        throw new IllegalStateException('Must be overridden with RegularFileProperty or DirectoryProperty')
    }

    // Diff specific
    abstract @Input Property<Boolean> getDiff()
    abstract @Input Property<Boolean> getAutoHeader()
    abstract @Input @Optional Property<Integer> getContext()
    abstract @Input @Optional Property<String> getArchiveModified()

    @Inject
    BaseDiffTask(Problems problems) {
        super(problems)

        if (this.modified instanceof DirectoryProperty)
            this.archiveModified.unset().disallowChanges()

        this.diff.convention(false)
        this.autoHeader.convention(false)
    }

    @Override
    void exec() {
        //region Diff specific
        if (this.diff.get())
            this.args('--diff')
        if (this.autoHeader.get())
            this.args('--auto-header')
        if (this.context.present)
            this.args('--context', this.context.get())
        if (this.archiveModified.present)
            this.args('--archive-modified', this.archiveModified.get())
        //endregion

        this.args(
            this.modified.locationOnly.map(this.problems.ensureFileLocation()).get().asFile.absolutePath
        )

        super.exec()
    }
}
