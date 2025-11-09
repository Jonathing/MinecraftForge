package net.minecraftforge.forge.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLanguageVersion

import javax.inject.Inject

abstract class BootstrapShimConfig extends DefaultTask {
    abstract @Input Property<String> getMainClass()
    abstract @Input Property<JavaLanguageVersion> getJavaVersion()
    abstract @Input ListProperty<String> getArgs()

    abstract @OutputFile RegularFileProperty getOutput()

    abstract @Inject ProjectLayout getLayout()

    @Inject
    BootstrapShimConfig() {
        output.convention(layout.buildDirectory.file('libs/bootstrap-shim.properties'))

        if (project.pluginManager.hasPlugin('java'))
            javaVersion.convention(project.extensions.findByType(JavaPluginExtension).toolchain.languageVersion)
    }

    @TaskAction
    protected void exec() {
        var cfg = new CleanProperties()
        cfg['Main-Class'] = mainClass.get()
        cfg['Java-Version'] = javaVersion.get().toString()
        cfg['Arguments'] = String.join(' ', args.get())
        cfg.store(output.asFile.get())
    }
}
