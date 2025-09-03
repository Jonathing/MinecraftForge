package net.minecraftforge.forgedev.tasks.mappings

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.util.file.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@CompileStatic
abstract class LegacyApplyMappings extends DefaultTask implements ForgeDevTask {
    protected abstract @InputFiles @Classpath ConfigurableFileCollection getClasspath()

    abstract @Input Property<Boolean> getJavadocs()
    abstract @Input Property<Boolean> getLambdas()

    abstract @InputFile RegularFileProperty getInput()
    abstract @InputFile RegularFileProperty getMappingsZip()
    abstract @OutputFile RegularFileProperty getOutput()

    @Inject
    LegacyApplyMappings() {
        this.classpath.from(
            this.forgedev.getTool(Tools.FASTCSV)
        )

        this.output.convention(this.defaultOutputFile)

        this.javadocs.convention(false)
        this.lambdas.convention(false)
    }

    protected abstract @Inject WorkerExecutor getWorkerExecutor()

    @TaskAction
    void exec() {
        final work = this.workerExecutor.classLoaderIsolation {
            it.classpath.from(this.classpath)
        }

        work.submit(Action) {
            it.javadocs.set this.javadocs
            it.lambdas.set this.lambdas

            it.input.set this.input
            it.mappingsZip.set this.mappingsZip
            it.output.set this.output
        }

        work.await()
    }

    @CompileStatic
    protected static abstract class Action implements WorkAction<Parameters> {
        @CompileStatic
        static interface Parameters extends WorkParameters {
            Property<Boolean> getJavadocs()
            Property<Boolean> getLambdas()

            RegularFileProperty getInput()
            RegularFileProperty getMappingsZip()
            RegularFileProperty getOutput()
        }

        @Inject
        Action() {}

        @Override
        void execute() {
            final javadocs = this.parameters.javadocs.getOrElse(false)
            final lambdas = this.parameters.lambdas.getOrElse(false)

            final input = this.parameters.input.get().asFile
            final mappingsZip = this.parameters.mappingsZip.get().asFile
            final output = this.parameters.output.get().asFile

            var names = MCPNames.load(mappingsZip)
            try (var zin = new ZipFile(input)) {
                try (var fos = new FileOutputStream(output)
                     var out = new ZipOutputStream(fos)) {
                    for (var entry : { zin.entries().asIterator() } as Iterable<? extends ZipEntry>) {
                        out.putNextEntry(FileUtils.getStableEntry(entry.name))
                        if (!entry.name.endsWith('.java')) {
                            zin.getInputStream(entry).transferTo(out)
                        } else {
                            out.write(names.rename(zin.getInputStream(entry), javadocs, lambdas).getBytes(StandardCharsets.UTF_8))
                        }
                        out.closeEntry()
                    }
                }
            }
        }
    }
}
