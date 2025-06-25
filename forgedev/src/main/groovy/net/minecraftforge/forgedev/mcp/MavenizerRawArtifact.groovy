package net.minecraftforge.forgedev.mcp

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

@CompileStatic
abstract class MavenizerRawArtifact extends JavaExec implements ForgeDevTask {
    private final ForgeDevProblems problems

    // Mavenizer
    abstract @InputDirectory @PathSensitive(PathSensitivity.ABSOLUTE) DirectoryProperty getCaches()

    // MCP Task
    abstract @Input Property<String> getArtifact() // can also be just the version
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input @Optional Property<String> getPipeline()
    abstract @Input Property<Boolean> getSrgNames()

    static TaskProvider<MavenizerRawArtifact> register(Project project, String pipeline, Provider<String> artifact, Provider<Boolean> srg) {
        project.tasks.register("raw${pipeline.capitalize()}Jar${srg ? 'Srg' : ''}", MavenizerRawArtifact) { task ->
            task.output.set(task.layout.buildDirectory.file("${task.name}.jar"))
            task.pipeline.set(pipeline)
            task.artifact.set(artifact)
            task.srgNames.set(srg)
        }
    }

    @Inject
    MavenizerRawArtifact(Problems problems) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.MAVENIZER))
        this.mainClass.convention(Constants.MAVENIZER_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.MAVENIZER_JAVA))

        var defaultDirectory = this.objectFactory.directoryProperty().value(this.globalCaches.dir('mavenizer').map(this.problems.ensureFileLocation()))
        this.caches.convention(defaultDirectory.dir('cache').map(this.problems.ensureFileLocation()))
        this.output.convention(this.layout.buildDirectory.file('forgedev/setupMCP.jar'))
    }

    protected abstract @Inject ProjectLayout getLayout()

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
            '--raw'
        )

        if (this.pipeline.present)
            this.args('--pipeline', this.pipeline.get())
        if (this.srgNames.getOrElse(false))
            this.args('--searge')
        //endregion

        super.exec()
    }
}
