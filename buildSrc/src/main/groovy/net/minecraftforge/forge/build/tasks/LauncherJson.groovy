package net.minecraftforge.forge.build.tasks

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import net.minecraftforge.forge.build.values.LibraryInfo
import net.minecraftforge.forge.build.values.MavenInfo
import net.minecraftforge.forgedev.tasks.patching.binary.ApplyBinPatches
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

import javax.inject.Inject
import java.nio.file.Files

abstract class LauncherJson extends DefaultTask {
    @OutputFile abstract RegularFileProperty getOutput()
    @InputFiles abstract ConfigurableFileCollection getInput()
    @Input Map<String, Object> json = new LinkedHashMap<>()

    LauncherJson() {
        output.convention(project.layout.buildDirectory.file('libs/version.json'))

        dependsOn(project.tasks.universalJar)
        input.from(project.tasks.universalJar.archiveFile)
        input.from(project.configurations.installer)
        input.from(project.configurations.installerextra)
        configure {
            def mc    = project.rootProject.ext.minecraftVersion
            def forge = project.rootProject.ext.forgeVersion
            def timestamp = Util.iso8601Now()
            json.putAll([
                _comment: [
                    "Please do not automate the download and installation of Forge.",
                    "Our efforts are supported by ads from the download page.",
                    "If you MUST automate this, please consider supporting the project through https://www.patreon.com/LexManos/"
                ],
                id: "$mc-$project.name-$forge",
                time: timestamp,
                releaseTime: timestamp,
                inheritsFrom: mc,
                type: 'release',
                logging: [:],
                mainClass: '',
                libraries: []
            ] as LinkedHashMap)

            [
                project.tasks.universalJar
            ].forEach { packed ->
                dependsOn(packed)
                input.from packed.archiveFile
            }

            def patched = project.tasks.applyClientBinPatches
            dependsOn(patched)
            input.from patched.output
        }
    }

    @TaskAction
    protected void exec() {
        var packed = (AbstractArchiveTask) project.tasks.universalJar
        def info = Util.getMavenInfoFromTask(packed)
        json.libraries.add([
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

        var classifier = 'client'
        var genned = project.tasks.applyClientBinPatches
        info = Util.getMavenInfoFromTask(genned, classifier)
        json.libraries.add([
            name: info.name,
            downloads: [
                artifact: [
                    path: info.path,
                    url: "",
                    sha1: genned.output.get().asFile.sha1(),
                    size: genned.output.get().asFile.length()
                ]
            ]
        ])

        json.libraries.addAll(Util.getArtifacts(project.configurations.installerextra).values())
        json.libraries.addAll(Util.getArtifacts(project.configurations.installer).values())
        Files.writeString(output.get().asFile.toPath(), new JsonBuilder(json).toPrettyString())
    }
}
