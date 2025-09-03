package net.minecraftforge.forgedev.tasks.mcp

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.ForgeDevPlugin
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.tasks.ToolExec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.InputDirectory
import org.jetbrains.annotations.MustBeInvokedByOverriders

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class MavenizerExec extends ToolExec {
    // Mavenizer
    protected abstract @InputDirectory DirectoryProperty getCaches()

    @Inject
    MavenizerExec() {
        super(Tools.MAVENIZER)

        this.caches.convention(this.defaultToolDir.dir('cache').map(this.problems.ensureFileLocation()))
    }

    @MustBeInvokedByOverriders
    protected void addArguments() {
        //region Mavenizer
        this.args(
            '--cache', this.caches.get().asFile.absolutePath,
            '--jdk-cache', this.caches.dir('jdks').get().asFile.absolutePath
        )
        //endregion
    }
}
