package net.minecraftforge.forge.build

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forge.build.tasks.Util
import net.minecraftforge.forge.build.tasks.WriteManifest
import net.minecraftforge.gradleutils.shared.Closures
import net.minecraftforge.gradleutils.shared.SharedUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject
import org.gradle.plugins.ide.eclipse.model.EclipseModel

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class BuildSharedPlugin implements Plugin<Project> {
    static {
        Util.init()
    }

    protected abstract @Inject ProjectLayout getLayout()

    @Inject
    BuildSharedPlugin() { }

    @Override
    void apply(Project project) {
        project.group = 'net.minecraftforge'

        SharedUtil.runFirst(project, project.tasks.register("generateResources")).tap { task ->
            project.tasks.named('processResources', ProcessResources) {
                it.dependsOn(task)
            }
        }

        project.pluginManager.withPlugin('java', javaPlugin -> {
            project.tasks.withType(Javadoc).configureEach(task ->
                task.options(Closures.<CoreJavadocOptions>consumer(options -> {
                    options.memberLevel = JavadocMemberLevel.PUBLIC
                    options.addBooleanOption('Xdoclint:all', true)
                    options.addBooleanOption('-Xdoclint:missing', true)
                }))
            )

            project.tasks.withType(JavaCompile).configureEach(task -> {
                // Needed because we merge the output of this with the output of the compile task. And gradle detects downstream tasks using the output without a hard dep
                task.dependsOn(
                    project.tasks.named('generateResources'),
                    project.tasks.named('processResources')
                )

                task.options.tap {
                    warnings = false // Shutup deprecated for removal warnings
                    forkOptions.jvmArgs << '-Xmx6G' // Needed to make compiling faster, and not run out of heap space in some cases.
                }
            })

            // Merge the resources and classes into the same directory. We'll need to split them at runtime because
            // Minecraft and Forge are in the same sourceSet as they are inter dependent.. for now..
            project.extensions.getByType(JavaPluginExtension).sourceSets.configureEach(sourceSet -> {
                var dir = layout.buildDirectory.dir("classes/java/${sourceSet.name}")
                sourceSet.output.resourcesDir = dir
                sourceSet.java.destinationDirectory.set(dir)
            })
        })

        project.pluginManager.withPlugin('eclipse', eclipsePlugin -> {
            var eclipse = project.getExtensions().getByType(EclipseModel)

            // Run when importing the project
            eclipse.synchronizationTasks(
                project.tasks.named('eclipseClasspath', GenerateEclipseClasspath),
                project.tasks.named('eclipseProject', GenerateEclipseProject)
            )
        })

        project.afterEvaluate { this.finish(it) }
    }

    private void finish(Project project) {
        // We need to write the manifest to the binary file so we have properly versioned packaged at dev time.
        var jar = project.pluginManager.hasPlugin('net.minecraftforge.forgedev') ? 'universalJar' : 'jar'
        WriteManifest.register(project, project.getTasks().named(jar, Jar))
    }
}
