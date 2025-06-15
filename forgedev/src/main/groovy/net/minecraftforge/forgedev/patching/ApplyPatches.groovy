package net.minecraftforge.forgedev.patching

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

@CompileStatic
abstract class ApplyPatches extends DiffPatchExec {
    abstract @Input Property<Boolean> getFailOnError()

    @Override
    abstract @InputDirectory @Optional @PathSensitive(PathSensitivity.ABSOLUTE) DirectoryProperty getPatches()

    @Inject
    ApplyPatches(Problems problems) {
        super(problems)

        this.failOnError.convention(true)

        this.logLevel.convention('warn')
        this.patch.convention(true)
    }

    abstract @Inject FileSystemOperations getFileSystemOperations()

    @Override
    void exec() {
        if (!this.patches.isPresent()) {
            this.fileSystemOperations.copy(spec -> spec
                .from(this.base)
                .into(this.output)
            )
            return
        }

        super.exec()

        var result = this.executionResult.get()
        var exitValue = result.exitValue
        if (exitValue !== 0) {
            // patches failed
            if (exitValue !== 1)
                result.rethrowFailure()

            // some other error
            if (this.failOnError.get())
                result.assertNormalExitValue()
        }
    }
}
