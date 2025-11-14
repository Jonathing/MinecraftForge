package net.minecraftforge.forge.build.tasks

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import net.minecraftforge.forge.build.values.LibraryInfo
import net.minecraftforge.forge.build.values.MinimalResolvedArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
abstract class InstallerJson extends DefaultTask {
    private static final String LOGO = 'big_logo.png'
    private static final String MIRRORS = 'https://files.minecraftforge.net/mirrors-2.0.json'

    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input ListProperty<MinimalResolvedArtifact> getArtifacts()
    abstract @Input @Optional MapProperty<String, LibraryInfo> getLibraries()
    abstract @Input MapProperty<String, Object> getJson()

    abstract @InputFile RegularFileProperty getIcon()
    abstract @Input Property<String> getLauncherJsonName()
    abstract @Input Property<String> getLogo()
    abstract @Input Property<String> getMirrors()
    abstract @Input Property<String> getWelcome()

    protected abstract @Inject ProviderFactory getProviders()
    protected abstract @Inject ProjectLayout getLayout()

    InstallerJson() {
        launcherJsonName.convention('/version.json')
        logo.convention("/$LOGO".toString())
        mirrors.convention(MIRRORS)
        welcome.convention("Welcome to the ${project.name.capitalize()} installer.".toString())
        output.convention(layout.buildDirectory.file('libs/install_profile.json'))
    }

    @SafeVarargs
    final void addArtifacts(TaskProvider<? extends AbstractArchiveTask>... tasks) {
        dependsOn(tasks)
        inputs.files(tasks)
        for (var task in tasks) {
            artifacts.add(MinimalResolvedArtifact.from(project, task))
        }
    }

    void addLibraries(Provider<? extends Configuration> configuration) {
        addLibraries(configuration.get())
    }

    void addLibraries(Configuration configuration) {
        dependsOn(configuration.buildDependencies)
        inputs.files(configuration)
        libraries.putAll(LibraryInfo.from(project, configuration))
    }

    @TaskAction
    protected void exec() {
        var libraries = new TreeMap<String, LibraryInfo>(libraries.get())
        for (var packed in artifacts.get()) {
            libraries.put(packed.info().name(), new LibraryInfo(packed.info(), packed.file(), "https://maven.minecraftforge.net/${packed.info().path()}"))
        }

        var json = new HashMap<String, Object>(json.get())
        json.putAll([
            libraries: libraries,
            icon: "data:image/png;base64,${new String(Base64.encoder.encode(Files.readAllBytes(icon.asFile.get().toPath())))}",
            json: launcherJsonName.get(),
            logo: logo.get(),
            welcome: welcome.get()
        ])
        if (!mirrors.get().isEmpty())
            json.put('mirrorList', mirrors.get())

        Files.writeString(output.asFile.get().toPath(), new JsonBuilder(json).toPrettyString())
    }
}
