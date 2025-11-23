package net.minecraftforge.forge.build.tasks

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import net.minecraftforge.forge.build.values.LibraryInfo
import net.minecraftforge.forge.build.values.MinimalResolvedArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
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

abstract class InstallerJson extends DefaultTask {
    @OutputFile abstract RegularFileProperty getOutput()
    @InputFiles abstract ConfigurableFileCollection getInput()
    @Input @Optional final Map<String, Object> libraries = new LinkedHashMap<>()
    @Input Map<String, Object> json = new LinkedHashMap<>()
    @InputFile abstract RegularFileProperty getIcon()
    @Input abstract Property<String> getLauncherJsonName()
    @Input abstract Property<String> getLogo()
    @Input abstract Property<String> getMirrors()
    @Input abstract Property<String> getWelcome()

    InstallerJson() {
        launcherJsonName.convention('/version.json')
        logo.convention('/big_logo.png')
        mirrors.convention('https://files.minecraftforge.net/mirrors-2.0.json')
        welcome.convention("Welcome to the ${project.name.capitalize()} installer.")
        output.convention(project.layout.buildDirectory.file('libs/install_profile.json'))

        project.afterEvaluate {
            [
                project.tasks.universalJar,
                project.tasks.serverShimJar
            ].forEach { packed ->
                dependsOn(packed)
                input.from packed.archiveFile
            }
        }
    }

    @TaskAction
    protected void exec() {
        def libs = libraries
        [
            project.tasks.universalJar,
            project.tasks.serverShimJar
        ].forEach { AbstractArchiveTask packed ->
            def info = Util.getMavenInfoFromTask(packed)
            libs.put(info.name, [
                name: info.name,
                downloads: [
                    artifact: [
                        path: info.path,
                        url: "https://maven.minecraftforge.net/$info.path",
                        sha1: packed.archiveFile.get().asFile.sha1(),
                        size: packed.archiveFile.get().asFile.length()
                    ]
                ]
            ])
        }
        json.libraries = libs.values().sort{a,b -> a.name.compareTo(b.name)}
        json.icon = "data:image/png;base64," + new String(Base64.getEncoder().encode(Files.readAllBytes(icon.get().asFile.toPath())))
        json.json = launcherJsonName.get()
        json.logo = logo.get()
        if (!mirrors.get().isEmpty())
            json.mirrorList = mirrors.get()
        json.welcome = welcome.get()

        Files.writeString(output.get().getAsFile().toPath(), new JsonBuilder(json).toPrettyString())
    }
}
