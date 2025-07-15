package net.minecraftforge.forge.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.java.archives.internal.ManifestInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
abstract class WriteManifest extends DefaultTask {
    static TaskProvider<WriteManifest> register(Project project, Provider<? extends Jar> jar, SourceSet sourceSet) {
        project.tasks.register('writeManifest', WriteManifest, sourceSet).tap { task ->
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

    abstract @Input Property<byte[]> getInputBytes()
    abstract @OutputFile RegularFileProperty getOutput()

    @Inject
    WriteManifest(ObjectFactory objects, ProviderFactory providers, SourceSet sourceSet) {
        //@formatter:off
        this.output.convention(
            objects.directoryProperty()
                   .fileProvider(providers.provider { sourceSet.resources.sourceDirectories.first() }) // get resources folder
                   .file('META-INF/MANIFEST.MF')                                                  // resources/META-INF/MANIFEST.MF
                   .map { Files.createDirectories(it.asFile.parentFile.toPath()); it }   // ensure META-INF folder exists
        )
        //@formatter:on
    }

    @TaskAction
    void exec() {
        this.output.get().asFile.bytes = this.inputBytes.get()
    }
}
