package net.minecraftforge.forge.build

import de.undercouch.gradle.tasks.download.Download
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import net.minecraftforge.forgedev.tasks.filtering.LegacyFilterNewJar
import net.minecraftforge.forgedev.tasks.installertools.ExtractInheritance
import net.minecraftforge.forgedev.tasks.mcp.MavenizerMCPSetup
import net.minecraftforge.forgedev.tasks.obfuscation.LegacyReobfuscateJar
import net.minecraftforge.forgedev.tasks.patching.binary.CreateBinPatches
import net.minecraftforge.gradleutils.shared.Closures
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.CopySpec
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip

import javax.inject.Inject
import java.nio.file.Files

@SuppressWarnings('unused')
@CompileStatic
@PackageScope abstract class BuildForgePluginOld implements Plugin<Project> {
    protected abstract @Inject ProviderFactory getProviders()
    protected abstract @Inject ProjectLayout getLayout()
    protected abstract @Inject ArchiveOperations getArchiveOperations()

    @Inject
    BuildForgePluginOld() {}

    // NOTE: IF ANY CHANGES ARE MADE, PLEASE UPDATE THE BUILDSCRIPT.MD TO REFLECT THEM!
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

        project.pluginManager.withPlugin('net.minecraftforge.forgedev') {
            var setupMCP = tasks.named('setupMCP', MavenizerMCPSetup)

            var extractInheritance = tasks.register('extractInheritance', ExtractInheritance) { task ->
                task.dependsOn(setupMCP)
                task.group = 'Forge downloads'
                
                task.additionalArgs.add('--annotations')
                task.input.fileProvider(tasks.named('genJoinedBinPatches', CreateBinPatches).map { it.clean.singleFile })
                task.libraries.from(setupMCP.flatMap(MavenizerMCPSetup.&getLibrariesList).map {
                    Files.readAllLines(it.asFile.toPath()).stream().map(File.&new).toList()
                })
            }

            final jar = tasks.named('jar', Jar)
            final reobfJar = tasks.named('reobfJar', LegacyReobfuscateJar)

            final officialClassesJar = tasks.register('officialClassesJar', Zip) { task ->
                task.dependsOn(jar)

                task.destinationDirectory.set(layout.buildDirectory)
                task.archiveClassifier.set('official-classes')
                task.archiveExtension.set('jar')

                task.from(providers.provider { archiveOperations.zipTree(jar.flatMap(Jar.&getArchiveFile)) }) { CopySpec copy ->
                    copy.include('**/*.class')
                    copy.exclude('mcp/**')
                }
            }

            final filterJarNew = tasks.named('filterJarNew', LegacyFilterNewJar) { task ->
                task.dependsOn(officialClassesJar)

                task.input.set(officialClassesJar.flatMap(Zip.&getArchiveFile))
            }

            final filterJarNewSrg = tasks.register('filterJarNewSrg', LegacyFilterNewJar) { task ->
                task.dependsOn(reobfJar, filterJarNew)

                task.input.set(reobfJar.flatMap(LegacyReobfuscateJar.&getOutput))
            }
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
