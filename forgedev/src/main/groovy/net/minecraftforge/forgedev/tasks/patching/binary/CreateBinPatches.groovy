package net.minecraftforge.forgedev.tasks.patching.binary

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

import javax.inject.Inject

@CompileStatic
abstract class CreateBinPatches extends BinaryPatcherExec {
    // Create
    abstract @InputFiles ConfigurableFileCollection getCreate()
    abstract @InputFiles @Optional ConfigurableFileCollection getPatches()
    abstract @InputFiles @Optional ConfigurableFileCollection getSrg()

    @Inject
    CreateBinPatches() { }

    @Override
    protected void addArguments() {
        super.addArguments()

        if (!this.create.empty) {
            this.create.forEach {
                this.args('--create', it.absolutePath)
            }
        } else {
            throw new IllegalArgumentException('No create!')
        }

        this.patches.forEach {
            this.args('--patches', it.absolutePath)
        }

        this.srg.forEach {
            this.args('--srg', it.absolutePath)
        }
    }
}
