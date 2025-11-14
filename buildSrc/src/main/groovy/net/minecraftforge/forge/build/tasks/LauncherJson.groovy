package net.minecraftforge.forge.build.tasks

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import net.minecraftforge.forge.build.values.LibraryInfo
import net.minecraftforge.forge.build.values.MavenInfo
import net.minecraftforge.forgedev.tasks.patching.binary.ApplyBinPatches
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Jar

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
abstract class LauncherJson extends DefaultTask {
    private static final String COMMENT = '''
        Please do not automate the download and installation of Forge.
        Our efforts are supported by ads from the download page.
        If you MUST automate this, please consider supporting the project through https://www.patreon.com/LexManos/'''.stripIndent()

    abstract @Input MapProperty<String, Object> getJson()
    abstract @OutputFile RegularFileProperty getOutput()

    protected abstract @Input Property<Boolean> getOffline()

    abstract @Input Property<String> getProjectName()
    abstract @Input Property<String> getMinecraftVersion()
    abstract @Input Property<String> getForgeVersion()
    abstract @Input Property<String> getTimestamp()
    abstract @InputFile RegularFileProperty getPackedJar()
    abstract @Input Property<MavenInfo> getPackedJarInfo()
    abstract @InputFile RegularFileProperty getPatchedJar()
    abstract @Input Property<MavenInfo> getPatchedJarInfo()

    abstract @Input MapProperty<String, LibraryInfo> getLibraries()

    protected abstract @Inject ProviderFactory getProviders()
    protected abstract @Inject ProjectLayout getLayout()

    LauncherJson() {
        offline.convention(project.gradle.startParameter.offline).finalizeValue()

        addLibraries(project.configurations.named('installer'))
        addLibraries(project.configurations.named('installerextra'))
        output.convention(layout.buildDirectory.file('libs/version.json'))

        projectName.convention(project.name)

        var universalJarTask = project.tasks.named('universalJar', Jar)
        dependsOn(universalJarTask)
        packedJar.set(universalJarTask.flatMap(Jar.&getArchiveFile))
        packedJarInfo.set(MavenInfo.from(project, universalJarTask))

        var applyClientBinPatches = project.tasks.named('applyClientBinPatches', ApplyBinPatches)
        dependsOn(applyClientBinPatches)
        patchedJar.set(applyClientBinPatches.flatMap(ApplyBinPatches.&getOutput))
        patchedJarInfo.set(MavenInfo.from(project, 'client'))

        json.put('_comment', COMMENT.split('\n'))
        json.put('inheritsFrom', minecraftVersion)
        json.putAll([
            type: 'release',
            logging: [:],
            mainClass: '',
            libraries: []
        ])
    }

    @SafeVarargs
    final void addLibraries(Provider<? extends Configuration>... configuration) {
        for (var c in configuration) {
            addLibraries(c.get())
        }
    }

    final void addLibraries(Configuration... configuration) {
        for (var c in configuration) {
            addLibraries(c)
        }
    }

    void addLibraries(Configuration configuration) {
        dependsOn(configuration.buildDependencies)
        inputs.files(configuration)
        libraries.putAll(LibraryInfo.from(project, configuration))
    }

    // REMINDER: DO NOT REFERENCE THE PROJECT IN TASK EXEC
    @TaskAction
    protected void exec() {
        var timestamp = Util.iso8601Now()
        var json = new HashMap<String, ?>(json.get())
        json.putAll([
            id: "${minecraftVersion.get()}-${projectName.get()}-${forgeVersion.get()}",
            time: timestamp,
            releaseTime: timestamp
        ])

        var libraries = new ArrayList<LibraryInfo>(this.libraries.get().values())
        libraries.add(0, new LibraryInfo(packedJarInfo.get(), packedJar.asFile.get(), "https://maven.minecraftforge.net/${packedJarInfo.get().path()}"))
        libraries.add(0, new LibraryInfo(patchedJarInfo.get(), patchedJar.asFile.get(), "https://maven.minecraftforge.net/${patchedJarInfo.get().path()}"))
        for (var library in libraries) {
            library.validateUrl(getOffline().getOrElse(false))
        }
        json.put('libraries', libraries)

        Files.writeString(output.asFile.get().toPath(), new JsonBuilder(json).toPrettyString())
    }
}
