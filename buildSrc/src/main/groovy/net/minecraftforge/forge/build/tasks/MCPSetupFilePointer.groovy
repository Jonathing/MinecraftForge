package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.shadow.net.minecraftforge.util.data.json.JsonData
import net.minecraftforge.forgedev.tasks.mcp.MCPSetupFiles
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

@CompileStatic
abstract class MCPSetupFilePointer extends DefaultTask {
    abstract @InputFile RegularFileProperty getInput()
    abstract @Input Property<String> getEntry()
    abstract @OutputFile RegularFileProperty getOutput()

    protected abstract @Inject ProjectLayout getLayout()

    @Inject
    MCPSetupFilePointer() {
        output.convention(layout.buildDirectory.file("setupMCP/$name"))
    }

    @TaskAction
    @CompileDynamic
    protected void exec() {
        var setupFiles = JsonData.fromJson(input.asFile.get(), MCPSetupFiles)
        output.asFile.get().bytes = new File(setupFiles."${entry.get()}").bytes
    }
}
