package net.minecraftforge.forgedev.mcp

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevPlugin
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec
import org.jetbrains.annotations.MustBeInvokedByOverriders

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class MavenizerExec extends JavaExec implements ForgeDevTask {
    protected final ForgeDevProblems problems

    // Mavenizer
    protected abstract @InputDirectory DirectoryProperty getCaches()

    @Inject
    MavenizerExec(Problems problems) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.MAVENIZER))
        this.mainClass.convention(Constants.MAVENIZER_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.MAVENIZER_JAVA))

        var toolDirectory = this.objectFactory.directoryProperty().value(this.globalCaches.dir('mavenizer').map(this.problems.ensureFileLocation()))
        this.caches.convention(toolDirectory.dir('cache').map(this.problems.ensureFileLocation()))
    }

    @MustBeInvokedByOverriders
    protected void addArguments() {
        //region Mavenizer
        this.args(
            '--cache', this.caches.get().asFile.absolutePath,
            "--jdk-cache", this.caches.dir("jdks").get().asFile.absolutePath
        )
        //endregion
    }

    @Override
    final void exec() {
        if (this.args.empty) // If the consumer hasn't manually set the command line arguments, add what we need.
            addArguments()

        ForgeDevPlugin.LOGGER.info('{} {}', this.classpath.asPath, this.args.join(' '))

        super.exec()
    }
}
