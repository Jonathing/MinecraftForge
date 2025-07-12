package net.minecraftforge.forge.gradle.tasks

import net.minecraftforge.forge.gradle.properties.CleanProperties
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

abstract class CopyEclipseSettings extends DefaultTask {
    abstract @InputFiles ConfigurableFileCollection getInputFiles()
    abstract @OutputFiles ConfigurableFileCollection getOutputFiles()

    @Inject
    CopyEclipseSettings() {
        final properties = this.project.rootProject.fileTree('ide/eclipse/template/.settings/').matching { include '**/*.prefs' }
        this.onlyIf { !properties.empty }

        this.inputFiles.setFrom(properties)
        this.outputFiles.setFrom(properties.files.collect { file -> layout.projectDirectory.file("settings/${file.name}") })
    }

    protected abstract @Inject ProjectLayout getLayout()
    protected abstract @Inject WorkerExecutor getWorkerExecutor()

    @TaskAction
    void exec() {
        final work = this.workerExecutor.noIsolation()

        for (final input in this.inputFiles) {
            final output = layout.projectDirectory.file(".settings/${input.name}")

            work.submit(CopySettingAction) {
                it.input.set(input)
                it.output.set(output)
            }
        }

        work.await()
    }

    protected static abstract class CopySettingAction implements WorkAction<Parameters> {
        static interface Parameters extends WorkParameters {
            @InputFile RegularFileProperty getInput()
            @InputFile RegularFileProperty getOutput()
        }

        @Inject
        CopySettingAction() { }

        @Override
        void execute() {
            var file = this.parameters.input.get().asFile
            var target = this.parameters.output.get().asFile
            var temp = new CleanProperties().load(file)
            var exst = new CleanProperties().load(target)
            exst.put('eclipse.preferences.version', '1')
            exst.putAll(temp)
            exst.store(target)
        }
    }
}
