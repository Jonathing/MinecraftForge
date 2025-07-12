package net.minecraftforge.forgedev.patching.diff

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevPlugin
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class BaseDiffPatchExec extends JavaExec implements ForgeDevTask {
    @PackageScope final ForgeDevProblems problems

    /* CLI FLAGS - See io.codechicken.diffpatch.cli.DiffPatchCli#mainI, or run --help on the fat jar */

    // Utility
    abstract @Input @Console Property<Boolean> getVerbose()
    abstract @Input @Optional @Console Property<String> getLogLevel()
    abstract @Input @Console Property<Boolean> getSummary()

    // Shared
    FileSystemLocationProperty<? extends FileSystemLocation> getInput() {
        throw new IllegalStateException('Must be overridden with RegularFileProperty or DirectoryProperty')
    }
    FileSystemLocationProperty<? extends FileSystemLocation> getOutput() {
        throw new IllegalStateException('Must be overridden with RegularFileProperty or DirectoryProperty')
    }
    abstract @Input @Optional Property<String> getArchive()
    abstract @Input @Optional Property<String> getArchiveBase()
    abstract @Input @Optional Property<String> getBasePathPrefix()
    abstract @Input @Optional Property<String> getModifiedPathPrefix()
    abstract @Input @Optional Property<String> getLineEndings()

    @Inject
    BaseDiffPatchExec(Problems problems) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.DIFFPATCH))
        this.mainClass.convention(Constants.DIFFPATCH_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.DIFFPATCH_JAVA))

        if (this.input instanceof DirectoryProperty)
            this.archiveBase.unset().disallowChanges()
        if (this.output instanceof DirectoryProperty)
            this.archive.unset().disallowChanges()

        this.verbose.convention(false)
        this.summary.convention(false)
    }

    protected void addArguments() {
        //region Utility
        if (this.verbose.get())
            this.args('--verbose')
        if (this.logLevel.present)
            this.args('--log-level', this.logLevel.get())
        if (this.summary.get())
            this.args('--summary')
        //endregion

        //region Shared
        if (this.output.present)
            this.args('--output', this.output.locationOnly.map(this.problems.ensureFileLocation()).get().asFile.absolutePath)
        if (this.archive.present)
            this.args('--archive', this.archive.get())
        if (this.archiveBase.present)
            this.args('--archive-base', this.archiveBase.get())
        if (this.basePathPrefix.present)
            this.args('--base-path-prefix', this.basePathPrefix.get())
        if (this.modifiedPathPrefix.present)
            this.args('--modified-path-prefix', this.modifiedPathPrefix.get())
        if (this.lineEndings.present)
            this.args('--line-endings', this.lineEndings.get())
        //endregion
    }

    @Override
    void exec() {
        if (this.args.empty) // If the consumer hasn't manually set the command line arguments, add what we need.
            addArguments()

        ForgeDevPlugin.LOGGER.info('{} {}', this.classpath.asPath, this.args.join(' '))

        super.exec()
    }
}
