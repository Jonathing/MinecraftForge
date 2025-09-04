package net.minecraftforge.forgedev.tasks.patching.diff

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

@CompileStatic
abstract class GeneratePatches extends BaseDiffTask {
    private final RegularFileProperty input = this.objectFactory.fileProperty()
    private final RegularFileProperty modified = this.objectFactory.fileProperty()
    private final RegularFileProperty output = this.objectFactory.fileProperty()

    @InputFile RegularFileProperty getInput() { this.input }
    @InputFile RegularFileProperty getModified() { this.modified }
    @OutputFile RegularFileProperty getOutput() { this.output }

}
