package net.minecraftforge.forgedev.compat

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.annotations.ApiStatus

import javax.inject.Inject

@CompileStatic
@Deprecated
@ApiStatus.ScheduledForRemoval
@DisableCachingByDefault(
    because = 'Files that this task depends on cannot be cached'
)
abstract class LegacyMergeFilesTask extends DefaultTask implements ForgeDevTask {
    abstract @InputFiles ConfigurableFileCollection getFilesToMerge()
    protected abstract @Input @Optional Property<String> getAdditionalData()

    abstract @Input Property<String> getSeparationComment()

    void addAdditionalData(String entry) {
        var existing = this.additionalData.present ? this.additionalData.get() + this.separationComment.get() : ''
        this.additionalData.set(existing + entry)
    }

    abstract @OutputFile RegularFileProperty getOutput()

    @Inject
    LegacyMergeFilesTask() {
        this.separationComment.convention('\n#============================================================\n')

        // TODO [ForgeDev] caching of this task (or delete it)
        this.outputs.upToDateWhen { false }
    }

    protected abstract @Inject ProviderFactory getProviders()

    @TaskAction
    void exec() {
        var buffer = new StringBuilder()
        for (var config in this.filesToMerge.files) {
            var entries = config.text

            if (buffer.length() !== 0)
                buffer.append('\n#============================================================\n')
            buffer.append(entries)
        }

        if (this.additionalData.present)
            buffer.append(this.additionalData.get())

        this.output.get().asFile.bytes = buffer.toString().bytes
    }
}
