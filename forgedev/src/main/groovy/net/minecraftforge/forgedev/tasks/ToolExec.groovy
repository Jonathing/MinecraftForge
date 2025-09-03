package net.minecraftforge.forgedev.tasks

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.jetbrains.annotations.MustBeInvokedByOverriders

import javax.inject.Inject

@CompileStatic
abstract class ToolExec extends JavaExec implements ForgeDevTask {
    protected final ForgeDevProblems problems

    // NOTE: This does not need to be the working directory
    // It's recommended to use this directory for shit like caches and outputs
    protected final @Internal DirectoryProperty defaultToolDir

    @Inject
    ToolExec(Problems problems, Tools tool) {
        this.problems = new ForgeDevProblems(problems, this.providerFactory)

        this.defaultToolDir = this.objectFactory.directoryProperty().value(
            this.forgedev.globalCaches.dir(tool.name().toLowerCase(Locale.ENGLISH)).map(this.problems.ensureFileLocation())
        ).tap { disallowChanges(); finalizeValueOnRead() }

        this.classpath = this.objectFactory.fileCollection().from(this.forgedev.getTool(tool))
        this.mainClass.convention(Objects.requireNonNull(tool.mainClass, 'Tool must have a main class'))
        this.javaLauncher.convention(Util.launcherForStrictly(this.javaToolchainService, tool.javaVersion))
    }

    @MustBeInvokedByOverriders
    protected abstract void addArguments()

    @Override
    @MustBeInvokedByOverriders
    void exec() {
        // If the consumer hasn't manually set the command line arguments, add what we need.
        if (this.args.empty)
            this.addArguments()

        this.logger.info('{} {}', this.classpath.asPath, String.join(' ', this.args))

        super.exec()
    }

    protected final void args(String arg, Iterable<? extends File> files) {
        for (var file in files)
            this.args(arg, file)
    }

    protected final void args(String arg, FileSystemLocationProperty<? extends FileSystemLocation> fileProvider) {
        this.args(arg, fileProvider.locationOnly)
    }

    protected final void args(String arg, Provider<?> provider) {
        final value = provider.get().with(true) {
            it instanceof FileSystemLocation ? it.asFile : it
        }

        this.args(arg, String.valueOf(value))
    }

    protected final void argOnlyIf(String arg, Property<Boolean> onlyIf) {
        this.argOnlyIf(arg) { onlyIf.present && onlyIf.getOrElse(false) }
    }

    protected final void argOnlyIf(String arg, Spec<? extends ToolExec> onlyIf) {
        if (onlyIf.isSatisfiedBy(this))
            this.args(arg)
    }
}
