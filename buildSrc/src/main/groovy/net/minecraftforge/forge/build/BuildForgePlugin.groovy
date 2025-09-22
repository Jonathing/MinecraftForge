package net.minecraftforge.forge.build

import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import net.minecraftforge.forge.build.tasks.DownloadLibraries
import net.minecraftforge.forge.build.tasks.ExtractFile
import net.minecraftforge.forgedev.tasks.installertools.ExtractInheritance
import net.minecraftforge.forgedev.tasks.patching.binary.CreateBinPatches
import net.minecraftforge.gradleutils.shared.Closures
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
@PackageScope abstract class BuildForgePlugin implements Plugin<Project> {
    protected abstract @Inject ProjectLayout getLayout()

    @Inject
    BuildForgePlugin() {}

    @Override
    @CompileDynamic
    void apply(Project project) {
        project.pluginManager.apply('de.undercouch.download')

        final tasks = project.tasks

        tasks.named('check') { task ->
//            task.dependsOn(tasks.named('checkJarCompatibility'))
        }

        registerDownload(tasks, 'crowdin', false) {
            src 'https://files.minecraftforge.net/crowdin.zip'
            dest layout.buildDirectory.file('crowdin.zip')
            useETag 'all'
            onlyIfModified true
            quiet true
        }

        var downloadVersionManifest = registerDownload(tasks, 'versionManifest', true) {
            src 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
            dest layout.buildDirectory.file('versions/version_manifest.json')
            useETag 'all'
            onlyIfModified true
            quiet true
        }

        var downloadJson = registerDownload(tasks, 'json', true) {
            dependsOn downloadVersionManifest.get()
            inputs.file downloadVersionManifest.map(Download.&getDest)
            src downloadVersionManifest.map { it.dest.json.versions.find { it.id == project.minecraftVersion }?.url }
            dest layout.buildDirectory.file("versions/${project.minecraftVersion}/version.json")
            useETag 'all'
            onlyIfModified true
            quiet true
        }

        var downloadClientRaw = registerDownload(tasks, 'clientRaw', true) {
            dependsOn downloadJson.get()
            inputs.file downloadJson.map(Download.&getDest)
            src downloadJson.map { it.dest.json.downloads?.client?.url }
            dest layout.buildDirectory.file("versions/${project.minecraftVersion}/client.jar")
            useETag 'all'
            onlyIfModified true
            quiet true
        }

        var downloadServerRaw = registerDownload(tasks, 'serverRaw', true) {
            dependsOn downloadJson
            inputs.file downloadJson.map(Download.&getDest)
            src downloadJson.map { it.dest.json.downloads?.server?.url }
            dest layout.buildDirectory.file("versions/${project.minecraftVersion}/server-bundled.jar")
            useETag 'all'
            onlyIfModified true
            quiet true
        }

        var extractServer = tasks.register('extractServer', ExtractFile) { task ->
            task.group = 'Forge downloads'

            task.dependsOn downloadServerRaw
            task.input.set(downloadServerRaw.map(Download.&getDest))
            task.target.set("META-INF/versions/${project.minecraftVersion}/server-${project.minecraftVersion}.jar")
            task.output.set(layout.buildDirectory.file("versions/${project.minecraftVersion}/server.jar"))
        }

        var downloadLibraries = tasks.register('downloadLibraries', DownloadLibraries) { task ->
            task.group = 'Forge downloads'

            task.dependsOn downloadJson.get()
            task.input.fileProvider(downloadJson.map(Download.&getDest))
            task.output.set(layout.buildDirectory.dir('libraries'))
        }

        var extractInheritance = tasks.register('extractInheritance', ExtractInheritance) { task ->
            task.group = 'Forge downloads'

            task.dependsOn downloadLibraries.get()
            task.additionalArgs.add('--annotations')
            task.input.fileProvider(tasks.named('genJoinedBinPatches', CreateBinPatches).map { it.clean.singleFile })
            task.libraries.from(downloadLibraries.flatMap(DownloadLibraries.&getLibrariesOutput).map { rf ->
                Files.readAllLines(rf.asFile.toPath()).stream().map(File.&new).toList()
            })
        }
    }

    private static TaskProvider<Download> registerDownload(
        TaskContainer tasks,
        String name,
        boolean redundant,
        @DelegatesTo(value = Download, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType, options = 'de.undercouch.gradle.tasks.download.Download')
        Closure<?> closure
    ) {
        return tasks.register("download${name.capitalize()}", Download) { task ->
            task.group = 'Forge downloads'
            if (redundant)
                task.doFirst { task.logger.warn("WARNING: Task '${task.name}' is doing work that is already done by the Mavenizer.") }

            Closures.invoke(closure, task)
        }
    }
}
