package net.minecraftforge.forgedev.patching

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Constants
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject
import java.nio.file.Files

@CompileStatic
@PackageScope abstract class DiffPatchExec extends JavaExec implements ForgeDevTask {
    private final ForgeDevProblems problems

    /* CLI FLAGS - See io.codechicken.diffpatch.cli.DiffPatchCli#mainI, or run --help on the fat jar */

    // Utility
    abstract @Input @Console Property<Boolean> getVerbose()
    abstract @Input @Optional @Console Property<String> getLogLevel()
    abstract @Input @Console Property<Boolean> getSummary()

    // Shared
    abstract @InputFile @PathSensitive(PathSensitivity.ABSOLUTE) RegularFileProperty getBase()       // no-flag arg 1
    abstract @InputDirectory @PathSensitive(PathSensitivity.ABSOLUTE) DirectoryProperty getPatches() // no-flag arg 2
    abstract @OutputFiles Property<File> getOutput()
    abstract @Input @Optional Property<String> getArchive()
    abstract @Input @Optional Property<String> getArchiveBase()
    abstract @Input @Optional Property<String> getBasePathPrefix()
    abstract @Input @Optional Property<String> getModifiedPathPrefix()

    // Diff specific
    abstract @Input Property<Boolean> getDiff()
    abstract @Input Property<Boolean> getAutoHeader()
    abstract @Input @Optional Property<Integer> getContext()
    abstract @Input @Optional Property<String> getArchiveModified()

    // Patch specific
    abstract @Input Property<Boolean> getPatch()
    @Internal Property<File> getRejects() { this.rejects }
    abstract @Input @Optional Property<String> getArchiveRejects()
    abstract @Input @Optional Property<Float> getFuzz()
    abstract @Input @Optional Property<Integer> getOffset()
    abstract @Input @Optional Property<String> getMode()
    abstract @Input @Optional Property<String> getPrefix()
    abstract @Input @Optional Property<String> getArchivePatches()

    void setRejects(Provider<?> rejects) {
        final value = rejects.get()

        if (value instanceof RegularFile)
            this.setRejects(value)
        else if (value instanceof Directory)
            this.setRejects(value)
        else
            this.rejects.set((File) rejects)
    }

    void setRejects(RegularFile rejects) {
        this.rejects.set(this.providerFactory.provider {
            rejects.asFile
        })
    }

    void setRejects(Directory rejects) {
        this.rejects.set(this.providerFactory.provider {
            rejects.asFile.tap {
                Files.createDirectories(it.toPath())
            }
        })
    }

    private final Property<File> rejects

    @Inject
    DiffPatchExec(Problems problems) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.classpath = this.objectFactory.fileCollection().from(this.getTool(Tools.DIFFPATCH))
        this.mainClass.convention(Constants.DIFFPATCH_MAIN)
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, Constants.DIFFPATCH_JAVA))

        this.verbose.convention(false)
        this.summary.convention(false)
        this.diff.convention(false)
        this.autoHeader.convention(false)
        this.patch.convention(false)
        this.rejects = this.objectFactory.property(File)
    }

    @Override
    void exec() {
        //region Utility
        if (this.verbose.get())
            this.args('--verbose')
        if (this.logLevel.isPresent())
            this.args('--log-level', this.logLevel.get())
        if (this.summary.get())
            this.args('--summary')
        //endregion

        //region Shared
        if (this.output.isPresent())
            this.args('--output', this.output.get().absolutePath)
        if (this.archive.isPresent())
            this.args('--archive', this.archive.get())
        if (this.archiveBase.isPresent())
            this.args('--archive-base', this.archiveBase.get())
        if (this.basePathPrefix.isPresent())
            this.args('--base-path-prefix', this.basePathPrefix.get())
        if (this.modifiedPathPrefix.isPresent())
            this.args('--modified-path-prefix', this.modifiedPathPrefix.get())
        //endregion

        //region Diff specific
        if (this.diff.get())
            this.args('--diff')
        if (this.autoHeader.get())
            this.args('--auto-header')
        if (this.context.isPresent())
            this.args('--context', this.context.get())
        if (this.archiveModified.isPresent())
            this.args('--archive-modified', this.archiveModified.get())
        //endregion

        //region Patch specific
        if (this.patch.get())
            this.args('--patch')
        if (this.rejects.isPresent())
            this.args('--reject', this.rejects.get().absolutePath)
        if (this.archiveRejects.isPresent())
            this.args('--archive-rejects', this.archiveRejects.get())
        if (this.fuzz.isPresent())
            this.args('--fuzz', this.fuzz.get())
        if (this.offset.isPresent())
            this.args('--offset', this.offset.get())
        if (this.mode.isPresent())
            this.args('--mode', this.mode.get())
        if (this.prefix.isPresent())
            this.args('--prefix', this.prefix.get())
        if (this.archivePatches.isPresent())
            this.args('--archive-patches', this.archivePatches.get())
        //endregion

        this.args(
            this.base.get().asFile.absolutePath,
            this.patches.get().asFile.absolutePath
        )

        println this.args.join(' ')

        super.exec()
    }
}
