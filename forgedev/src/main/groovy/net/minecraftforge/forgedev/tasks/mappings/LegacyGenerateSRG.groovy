package net.minecraftforge.forgedev.tasks.mappings

import de.siegmar.fastcsv.reader.CsvReader
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.IRenamer
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
import java.util.zip.ZipFile

@CompileStatic
abstract class LegacyGenerateSRG extends DefaultTask implements ForgeDevTask {
    protected abstract @InputFiles @Classpath ConfigurableFileCollection getClasspath()

    abstract @Input Property<IMappingFile.Format> getFormat()
    abstract @Input Property<Boolean> getNotch()
    abstract @Input Property<Boolean> getReverse()

    abstract @InputFile RegularFileProperty getSrg()
    abstract @InputFile RegularFileProperty getMappingsZip()
    abstract @OutputFile RegularFileProperty getOutput()

    LegacyGenerateSRG() {
        this.classpath.from(
            this.forgeDev.getTool(Tools.SRGUTILS),
            this.forgeDev.getTool(Tools.FASTCSV)
        )

        this.format.convention(IMappingFile.Format.TSRG2)
        this.notch.convention(false)
        this.reverse.convention(false)

        this.output.convention(this.defaultOutputFile)
    }

    protected abstract @Inject WorkerExecutor getWorkerExecutor()

    @TaskAction
    void exec() {
        final work = this.workerExecutor.classLoaderIsolation {
            it.classpath.from(this.classpath)
        }

        work.submit(Action) {
            it.format.set this.format
            it.notch.set this.notch
            it.reverse.set this.reverse

            it.srg.set this.srg
            it.mappingsZip.set this.mappingsZip
            it.output.set this.output
        }

        work.await()
    }

    protected static abstract class Action implements WorkAction<Parameters> {
        static interface Parameters extends WorkParameters {
            Property<IMappingFile.Format> getFormat()
            Property<Boolean> getNotch()
            Property<Boolean> getReverse()

            RegularFileProperty getSrg()
            RegularFileProperty getMappingsZip()
            RegularFileProperty getOutput()
        }

        @Inject
        Action() { }

        @Override
        void execute() {
            var input = IMappingFile.load(this.parameters.srg.get().asFile).with(true) {
                boolean notch = this.parameters.notch.getOrElse(false)

                // Reverse makes SRG->OBF, chain makes SRG->SRG
                return !notch ? it.reverse().chain(it) : it
            }

            var map = MappingData.load(this.parameters.mappingsZip.get().asFile)
            var ret = input.rename(map.renamer)

            ret.write(
                this.parameters.output.get().asFile.toPath(),
                this.parameters.format.get(),
                this.parameters.reverse.getOrElse(false)
            )
        }

        @CompileStatic
        private static final @Immutable class MappingData {
            Map<String, String> names, docs

            private IRenamer getRenamer() {
                new IRenamer() {
                    private String renameInternal(String value) {
                        MappingData.this.names.getOrDefault(value, value)
                    }

                    @Override
                    String rename(IMappingFile.IPackage value) {
                        this.renameInternal(value.mapped)
                    }

                    @Override
                    String rename(IMappingFile.IClass value) {
                        this.renameInternal(value.mapped)
                    }

                    @Override
                    String rename(IMappingFile.IField value) {
                        this.renameInternal(value.mapped)
                    }

                    @Override
                    String rename(IMappingFile.IMethod value) {
                        this.renameInternal(value.mapped)
                    }

                    @Override
                    String rename(IMappingFile.IParameter value) {
                        this.renameInternal(value.mapped)
                    }
                }
            }

            private static MappingData load(File data) throws IOException {
                final Map<String, String> names = [:]
                final Map<String, String> docs = [:]
                try (var zip = new ZipFile(data)) {
                    // Iterate over the enumeration instead of using a Stream, it's cleaner this way imo
                    var entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement()

                        // Not a CSV file? Skip.
                        if (!entry.name.endsWith('.csv')) continue

                        try (var reader = CsvReader.builder().ofNamedCsvRecord(new InputStreamReader(zip.getInputStream(entry)))) {
                            for (var row : reader) {
                                var header = row.header
                                var obf = header.contains('searge') ? 'searge' : 'param'
                                var searge = row.getField(obf)

                                names[searge] = row.getField('name')

                                if (header.contains('desc')) {
                                    String desc = row.getField('desc')
                                    if (!desc.blank)
                                        docs[searge] = desc
                                }
                            }
                        }
                    }
                }

                new MappingData(names, docs)
            }
        }
    }
}
