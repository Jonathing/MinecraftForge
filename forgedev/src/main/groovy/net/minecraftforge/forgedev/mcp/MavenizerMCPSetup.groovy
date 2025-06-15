package net.minecraftforge.forgedev.mcp

import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

abstract class MavenizerMCPSetup extends JavaExec implements ForgeDevTask {
    private final ForgeDevProblems problems

    // Mavenizer
    abstract @InputDirectory @PathSensitive(PathSensitivity.ABSOLUTE) DirectoryProperty getCaches()

    // MCP Task
    abstract @Input Property<String> getArtifact() // can also be just the version
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input @Optional Property<String> getPipeline()
    abstract @InputFile @Optional @PathSensitive(PathSensitivity.ABSOLUTE) RegularFileProperty getAccessTransformerConfig()
    abstract @InputFile @Optional @PathSensitive(PathSensitivity.ABSOLUTE) RegularFileProperty getSideAnnotationStripperConfig()
    abstract @Input @Optional Property<String> getParchment()

    @Inject
    MavenizerMCPSetup(Problems problems, ProjectLayout layout) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.MAVENIZER))
        this.mainClass.convention(Constants.MAVENIZER_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.MAVENIZER_JAVA))

        var defaultDirectory = this.objectFactory.directoryProperty().value(this.globalCaches.dir('mavenizer').map(this.problems.ensureDirectory()))
        this.caches.convention(defaultDirectory.dir('cache').map(this.problems.ensureDirectory()))
        this.output.convention(layout.buildDirectory.file('forgedev/setupMCP.jar'))
    }

    @Override
    void exec() {
        //region Mavenizer
        this.args(
            '--mcp',
            '--cache', this.caches.get().asFile.absolutePath,
            "--jdk-cache", this.caches.dir("jdks").get().asFile.absolutePath
        )
        //endregion

        //region MCP Task
        var artifact = this.artifact.get()
        this.args(
            artifact.contains(':') ? '--artifact' : '--version', artifact,
            '--output', this.output.get().asFile.absolutePath,
            '--mappings'
        )

        if (this.pipeline.present)
            this.args('--pipeline', this.pipeline.get())
        if (this.accessTransformerConfig.present)
            this.args('--at', this.accessTransformerConfig.get())
        if (this.sideAnnotationStripperConfig.present)
            this.args('--sas', this.sideAnnotationStripperConfig.get())
        if (this.parchment.present)
            this.args('--parchment', this.parchment.get())
        //endregion

        super.exec()
    }
}
