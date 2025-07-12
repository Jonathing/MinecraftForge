package net.minecraftforge.forgedev.mcp

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class MavenizerMCPTask extends MavenizerExec {
    // MCP Task
    abstract @Input Property<String> getArtifact() // can also be just the version
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input @Optional Property<String> getPipeline()

    @Inject
    MavenizerMCPTask(Problems problems) {
        super(problems)
    }

    @Override
    protected void addArguments() {
        super.addArguments()

        //region MCP Task
        var artifact = this.artifact.get()
        this.args(
            '--mcp',
            artifact.contains(':') ? '--artifact' : '--version', artifact,
            '--output', this.output.get().asFile.absolutePath,
        )

        if (this.pipeline.present)
            this.args('--pipeline', this.pipeline.get())
        //endregion
    }
}
