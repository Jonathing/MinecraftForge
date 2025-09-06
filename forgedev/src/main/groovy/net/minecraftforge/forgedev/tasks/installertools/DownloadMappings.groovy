package net.minecraftforge.forgedev.tasks.installertools

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.tasks.ToolExec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

@CompileStatic
abstract class DownloadMappings extends ToolExec {
    abstract @Input Property<String> getSide()
    abstract @Input Property<String> getVersion()

    abstract @OutputFile RegularFileProperty getOutput()

    @Inject
    DownloadMappings() {
        super(Tools.INSTALLERTOOLS)

        this.output.convention(this.getDefaultOutputFile('tsrg'))
    }

    @Override
    protected void addArguments() {
        this.args(
            '--task', 'DOWNLOAD_MOJMAPS',
            '--sanitize',
            '--version', this.version.get(),
            '--side', this.side.get(),
            '--output', this.output.asFile.get().absolutePath
        )

        super.addArguments()
    }
}
