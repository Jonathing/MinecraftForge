package net.minecraftforge.forgedev.tasks.mcp

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

import javax.inject.Inject

@CompileStatic
abstract class MavenizerMCPSetup extends MavenizerMCPTask {
    abstract @InputFile @Optional RegularFileProperty getAccessTransformerConfig()
    abstract @InputFile @Optional RegularFileProperty getSideAnnotationStripperConfig()
    abstract @Input @Optional Property<String> getParchment()

    @Inject
    MavenizerMCPSetup() {}

    @Override
    protected void addArguments() {
        super.addArguments()

        this.args('--mappings')

        if (this.accessTransformerConfig.present)
            this.args('--at', this.accessTransformerConfig.get())
        if (this.sideAnnotationStripperConfig.present)
            this.args('--sas', this.sideAnnotationStripperConfig.get())
        if (this.parchment.present)
            this.args('--parchment', this.parchment.get())
    }
}
