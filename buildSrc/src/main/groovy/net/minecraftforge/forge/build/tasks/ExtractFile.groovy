package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable

import javax.inject.Inject

@CompileStatic
abstract class ExtractFile extends DefaultTask {
    abstract @InputFile RegularFileProperty getInput()
    abstract @Input Property<String> getTarget()
    abstract @OutputFile RegularFileProperty getOutput()

    protected abstract @Inject ArchiveOperations getArchiveOperations()

    @TaskAction
    protected void exec() {
        final target = this.target.get()
        var zipTree = archiveOperations.zipTree(input).matching { PatternFilterable pattern ->
            pattern.include(target)
        }

        File entry
        try {
            entry = zipTree.singleFile
        } catch (IllegalStateException e) {
            throw new IllegalStateException("${this.input.asFile.get().absolutePath} does not contain $target", e)
        }

        output.asFile.get().tap { parentFile.mkdirs() }.bytes = entry.bytes
    }
}
