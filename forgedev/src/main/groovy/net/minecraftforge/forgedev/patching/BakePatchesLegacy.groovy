package net.minecraftforge.forgedev.patching

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject
import java.util.zip.ZipOutputStream

@CompileStatic
abstract class BakePatchesLegacy extends DefaultTask implements ForgeDevTask {
    abstract @InputDirectory @Optional DirectoryProperty getInput()
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input Property<String> getLineEnding()

    private final ForgeDevProblems problems

    @Inject
    BakePatchesLegacy(Problems problems, ProviderFactory providers) {
        this.problems = new ForgeDevProblems(problems, providers)

        this.lineEnding.convention(providers.provider(System.&lineSeparator))
    }

    protected abstract @Inject WorkerExecutor getWorkerExecutor()

    @TaskAction
    void exec() {
        if (this.input.present) {
            this.workerExecutor.classLoaderIsolation { it.classpath.from(this.getTool(Tools.DIFFPATCH)) }.submit(BakePatchesAction) { spec ->
                spec.input.set(this.input)
                spec.output.set(this.output)
                spec.lineEnding.set(this.lineEnding)
            }
        } else {
            // create empty zip for output
            try (var zos = new ZipOutputStream(new FileOutputStream(output.get().asFile))) { }
        }
    }

    @CompileStatic
    static abstract class BakePatchesAction implements WorkAction<Parameters> {
        @CompileStatic
        static interface Parameters extends WorkParameters {
            //@formatter:off
            @InputDirectory DirectoryProperty getInput()
            @OutputFile RegularFileProperty getOutput()
            @Input Property<String> getLineEnding()
            //@formatter:on
        }

        // TODO [ForgeDev] PR this as a CLI entry-point to DiffPatch
        //  The classloader isolation from the task ensures that this will not fail at runtime
        //  Once a new CLI entry point is in, remove this action AND the dependency from ForgeDev
        @Override
        void execute() {
            io.codechicken.diffpatch.cli.PatchOperation.bakePatches(
                io.codechicken.diffpatch.util.Input.MultiInput.folder(this.parameters.input.get().asFile.toPath()),
                io.codechicken.diffpatch.util.Output.MultiOutput.archive(io.codechicken.diffpatch.util.archiver.ArchiveFormat.ZIP, this.parameters.output.get().asFile.toPath()),
                this.parameters.lineEnding.get()
            )
        }
    }
}
