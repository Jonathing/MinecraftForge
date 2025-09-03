package net.minecraftforge.forgedev.tasks.compat

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

@CompileStatic
abstract class LegacyExtractExistingFiles extends DefaultTask implements ForgeDevTask {
    abstract @InputFile @SkipWhenEmpty RegularFileProperty getArchive()
    abstract @OutputDirectories ConfigurableFileCollection getTargets()

    protected abstract @Inject FileSystemOperations getFileSystemOperations()
    protected abstract @Inject ArchiveOperations getArchiveOperations()

    @TaskAction
    void exec() {
        final zip = this.archiveOperations.zipTree(this.archive)

        for (var directory in this.targets) {
            this.fileSystemOperations.copy(copy -> copy
                .from(zip)
                .into(directory)
                .setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
            )
        }
    }
}
