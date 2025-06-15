package net.minecraftforge.forgedev.compat

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

// TODO [ForgeDev] Consider using extraction as a tool in other tasks instead of this dedicated task
@CompileStatic
abstract class LegacyExtractZip extends DefaultTask {
    abstract @InputFile RegularFileProperty getInput()
    abstract @OutputDirectory DirectoryProperty getOutput()

    LegacyExtractZip() {
        // TODO [ForgeDev] Consider removing this
        this.outputs.upToDateWhen { false }
    }

    protected abstract @Inject FileSystemOperations getFileSystemOperations()
    protected abstract @Inject ArchiveOperations getArchiveOperations()

    @TaskAction
    void exec() {
        this.fileSystemOperations.copy(copy -> copy
            .from(this.archiveOperations.zipTree(this.input))
            .into(this.output)
        )
    }
}
