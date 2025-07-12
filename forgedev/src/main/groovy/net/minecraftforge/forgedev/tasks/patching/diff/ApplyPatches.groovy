package net.minecraftforge.forgedev.tasks.patching.diff

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

@CompileStatic
abstract class ApplyPatches extends BasePatchTask {
    abstract @Input Property<Boolean> getFailOnError()

    private final RegularFileProperty input
    private final DirectoryProperty patches
    private final RegularFileProperty output

    @InputFile RegularFileProperty getInput() { this.input }
    @InputDirectory @Optional DirectoryProperty getPatches() { this.patches }
    @OutputFile RegularFileProperty getOutput() { this.output }

    @Inject
    ApplyPatches(Problems problems) {
        super(problems)

        this.input = this.objectFactory.fileProperty()
        this.patches = this.objectFactory.directoryProperty()
        this.output = this.objectFactory.fileProperty()

        this.failOnError.convention(true)

        this.logLevel.convention('warn')
    }

    @Override
    void exec() {
        if (!this.patches.isPresent()) {
            this.output.get().asFile.bytes = this.input.get().asFile.bytes
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
