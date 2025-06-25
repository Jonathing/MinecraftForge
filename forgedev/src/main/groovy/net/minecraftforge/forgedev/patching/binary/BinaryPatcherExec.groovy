package net.minecraftforge.forgedev.patching.binary

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class BinaryPatcherExec extends JavaExec implements ForgeDevTask {
    @PackageScope final ForgeDevProblems problems

    // Shared
    abstract @InputFiles ConfigurableFileCollection getClean()
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @Input @Optional ListProperty<String> getPrefix()
    abstract @Input Property<Boolean> getPack200()
    @Deprecated abstract @Input Property<Boolean> getLegacy()

    @Inject
    @SuppressWarnings('GrDeprecatedAPIUsage') // setting convention "false" for legacy
    BinaryPatcherExec(Problems problems) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.BINPATCH))
        this.mainClass.convention(Constants.BINPATCH_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.BINPATCH_JAVA))

        this.pack200.convention(false)
        this.legacy.convention(false)
    }

    @Override
    void exec() {
        if (!this.clean.empty) {
            this.clean.forEach {
                this.args('--clean', it.absolutePath)
            }
        } else {
            throw new IllegalArgumentException('no clean!')
        }

        this.args('--output', this.output.get().asFile.absolutePath)

        if (this.prefix.present) {
            this.prefix.get().forEach {
                this.args('--prefix', it)
            }
        }

        if (this.pack200.getOrElse(false))
            this.args('--pack200')

        //noinspection GrDeprecatedAPIUsage
        if (this.legacy.getOrElse(false))
            this.args('--legacy')

        super.exec()
    }
}
