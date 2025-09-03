package net.minecraftforge.forgedev.tasks.mcp

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

import javax.inject.Inject

@CompileStatic
abstract class MavenizerSyncMappings extends MavenizerExec {
    // MCP Task
    abstract @Input Property<String> getVersion()
    abstract @Input @Optional Property<String> getParchmentVersion()
    protected abstract @Internal DirectoryProperty getOutput()

    @Inject
    MavenizerSyncMappings(Problems problems) {
        super(problems)

        this.output.convention(this.forgedev.mavenizerRepo)
    }

    //net.minecraft:mappings_channel:version@zip
    //--maven --artifact net.minecraft:mappings --version 1.21.7
    @Override
    protected void addArguments() {
        super.addArguments()

        this.args(
            '--maven',
            '--output', this.output.locationOnly.get().asFile.absolutePath,
            '--artifact', 'net.minecraft:mappings',
            '--version', this.version.get()
        )
        if (this.parchmentVersion.present) {
            this.args('--parchment', this.parchmentVersion.get())
        }
    }
}
