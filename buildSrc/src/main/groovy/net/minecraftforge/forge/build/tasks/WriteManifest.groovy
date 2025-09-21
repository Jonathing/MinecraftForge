package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.java.archives.internal.ManifestInternal
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

import javax.inject.Inject

@CompileStatic
abstract class WriteManifest extends DefaultTask {
    static TaskProvider<WriteManifest> register(Project project, TaskProvider<? extends Jar> jar) {
        project.tasks.register('writeManifest', WriteManifest).tap { task ->
            project.tasks.named('processResources', ProcessResources) {
                it.dependsOn(task)
                it.from(task) { CopySpec copy ->
                    // Take the output from this task and copy it into resources META-INF
                    copy.into('META-INF')

                    // Replace duplicate file if it exists
                    copy.duplicatesStrategy = DuplicatesStrategy.INCLUDE
                }
            }

            project.afterEvaluate {
                try (var os = new ByteArrayOutputStream()) {
                    // RATIONALE: ManifestInternal has not changed since Gradle 2.14
                    // Due to the hacky nature of needing the proper manifest in the resources, this is the only good way of doing this
                    // The DefaultManifest object cannot be serialized into the Gradle cache, and the normal Manifest interface does not have this method
                    // This should be the only Gradle internals we need to use in all of ForgeDev, thankfully
                    (jar.get().manifest as ManifestInternal).writeTo(os)
                    task.get().inputBytes.set(os.toByteArray())
                }
            }
        }
    }

    protected abstract @Input Property<byte[]> getInputBytes()
    protected abstract @OutputFile RegularFileProperty getOutput()

    @Inject
    WriteManifest(ProjectLayout layout) {
        // The output name is ALWAYS "MANIFEST.MF", and output cannot be changed
        this.output.value(layout.buildDirectory.file("${this.name}/MANIFEST.MF")).disallowChanges()
    }

    @TaskAction
    void exec() {
        this.output.get().asFile.bytes = this.inputBytes.get()
    }
}
