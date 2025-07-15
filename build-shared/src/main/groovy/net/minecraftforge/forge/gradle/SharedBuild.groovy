package net.minecraftforge.forge.gradle

import net.minecraftforge.forge.gradle.tasks.WriteManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaModel

class SharedBuild implements Plugin<Project> {
    public static String version

    @Override
    void apply(Project project) {
        // replacement for build_shared.gradle, less volatile
        // tap method is used since it makes the delegate the primary reference
        project.tap {
            apply plugin: 'eclipse'
            apply plugin: 'idea'
            apply plugin: 'net.minecraftforge.gradleutils'

            // extension references
            var java = extensions.getByType(JavaPluginExtension)
            var eclipse = extensions.getByType(EclipseModel)
            var idea = extensions.getByType(IdeaModel)

            group = 'net.minecraftforge'
            version = SharedBuild.version ?: extensions.extraProperties.get('VERSION')

            logger.lifecycle("Version: $version")

            // TODO [ForgeDev] Move to settings.gradle once FG6 is removed
            repositories.tap {
                mavenCentral()
                maven { url = 'https://maven.minecraftforge.net/' }
                maven { url = 'https://libraries.minecraft.net/' }
            }

            tasks.withType(Javadoc).configureEach { task ->
                task.options { StandardJavadocDocletOptions options ->
                    options.tags = [
                        'apiNote:a:<em>API Note:</em>',
                        'implSpec:a:<em>Implementation Requirements:</em>',
                        'implNote:a:<em>Implementation Note:</em>'
                    ]

                    options.addStringOption('Xdoclint:all,-missing', '-public')
                }
            }

            // We need to write the manifest to the binary file so we have properly versioned packaged at dev time.
            WriteManifest.register(
                project,
                tasks.named(plugins.findPlugin('net.minecraftforge.gradle.patcher') ? 'universalJar' : 'jar', Jar),
                java.sourceSets.main
            )

            tasks.register('generateResources') { task ->
                task.dependsOn 'writeManifest'
            }

            // Make sure out manifests get written before compiling the code, IDEA calls this task if you tell it to use the gradle build.
            tasks.withType(JavaCompile).configureEach { task ->
                task.dependsOn 'generateResources'
                task.dependsOn 'processResources' // Needed because we merge the output of this with the output of the compile task. And gradle detects downstream tasks using the output without a hard dep

                task.options.tap {
                    encoding = 'UTF-8' // Use the UTF-8 charset for Java compilation
                    warnings = false // Shutup deprecated for removal warnings
                    forkOptions.jvmArgs += '-Xmx6G' // Needed to make compiling faster, and not run out of heap space in some cases.
                }
            }

            // Merge the resources and classes into the same directory. We'll need to split them at runtime because
            // Minecraft and Forge are in the same sourceSet as they are inter dependent.. for now..
            java.sourceSets.each {
                def dir = layout.buildDirectory.dir("classes/java/$it.name")
                it.output.resourcesDir = dir
                it.java.destinationDirectory = dir
            }

            eclipse.tap {
                // Run everytime eclipse builds the code
                //autoBuildTasks('writeManifest')
                // Run when importing the project
                synchronizationTasks('generateResources', 'eclipseClasspath', 'eclipseProject')
            }

            idea.module.tap {
                downloadJavadoc = downloadSources = true
            }
        }
    }
}
