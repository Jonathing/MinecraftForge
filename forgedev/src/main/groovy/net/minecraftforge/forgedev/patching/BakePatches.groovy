package net.minecraftforge.forgedev.patching

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject
import java.util.zip.ZipOutputStream

@CompileStatic
abstract class BakePatches extends BasePatchBakingTask {
    private final DirectoryProperty input
    private final RegularFileProperty output

    @InputDirectory @Optional DirectoryProperty getInput() { input }
    @OutputFile RegularFileProperty getOutput() { output }

    @Inject
    BakePatches(Problems problems) {
        super(problems)

        this.input = this.objectFactory.directoryProperty()
        this.output = this.objectFactory.fileProperty()
    }

    @Override
    void exec() {
        if (!this.input.present) {
            // create empty zip for output
            new ZipOutputStream(new FileOutputStream(output.get().asFile)).close()
            return
        }

        super.exec()
    }
}
