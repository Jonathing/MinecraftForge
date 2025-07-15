package net.minecraftforge.forgedev.tasks.mcp

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
abstract class MavenizerMCPDataTask extends MavenizerExec {
    // MCP Task
    abstract @Input Property<String> getArtifact() // can also be just the version
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input Property<Boolean> getOfficial()
    abstract @Input @Optional Property<String> getParchment()
    abstract @Input Property<String> getKey()

    // Legacy
    abstract @Input Property<Boolean> getIsAllowEmpty()

    @Inject
    MavenizerMCPDataTask(Problems problems) {
        super(problems)

        this.output.convention(this.defaultOutputFile)
        this.official.convention(false)
        this.key.convention('mappings')

        this.isAllowEmpty.convention(false)
    }

    @Override
    protected void addArguments() {
        final official = this.official.getOrElse(false)
        if (official && this.parchment.present)
            throw new IllegalArgumentException('MavenizerMCPDataTask cannot use both official and parchment mappings')

        super.addArguments()

        //region MCP Data Task
        var artifact = this.artifact.get()
        this.args(
            '--mcp-data',
            artifact.contains(':') ? '--artifact' : '--version', artifact,
            '--output', this.output.get().asFile.absolutePath,
        )

        if (official)
            this.args('--mappings')
        else if (this.parchment.present)
            this.args('--parchment', this.parchment.get())

        this.args('--key', this.key.get())
        //endregion
    }

    @Override
    void exec() {
        super.exec()

        if (!this.output.get().asFile.exists() && this.isAllowEmpty.getOrElse(false)) {
            Files.createFile(this.output.get().asFile.toPath())
        }
    }
}
