package net.minecraftforge.forgedev.mcp

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

@CompileStatic
abstract class MavenizerRawArtifact extends MavenizerMCPTask {
    static TaskProvider<MavenizerRawArtifact> register(Project project, String pipeline, Provider<String> artifact, Provider<Boolean> srgNames) {
        project.tasks.register(getNameFor(pipeline, srgNames), MavenizerRawArtifact, pipeline, artifact, srgNames)
    }

    private static String getNameFor(String pipeline, Provider<Boolean> srgNames) {
        "raw${pipeline.capitalize()}Jar${srgNames.getOrElse(false) ? 'Srg' : ''}"
    }

    abstract @Input Property<Boolean> getSrgNames()

    @Inject
    MavenizerRawArtifact(Problems problems, ProjectLayout layout, String pipeline, Provider<String> artifact, Provider<Boolean> srg) {
        super(problems)

        this.output.set(layout.buildDirectory.file("${this.name}.jar"))
        this.pipeline.set(pipeline)
        this.artifact.set(artifact)
        this.srgNames.set(srg)
    }

    @Override
    protected void addArguments() {
        this.args('--raw')

        if (this.srgNames.getOrElse(false))
            this.args('--searge')
    }
}
