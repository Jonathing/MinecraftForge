package net.minecraftforge.forgedev.tasks.patching.binary

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles

import javax.inject.Inject

@CompileStatic
abstract class ApplyBinPatches extends BinaryPatcherExec {
    // Create
    abstract @InputFiles ConfigurableFileCollection getApply()
    abstract @Input Property<Boolean> getData()
    abstract @Input Property<Boolean> getUnpatched()

    @Inject
    ApplyBinPatches() {
        this.data.convention(false)
        this.unpatched.convention(false)
    }

    @Override
    protected void addArguments() {
        super.addArguments()

        if (!this.apply.empty) {
            this.apply.forEach {
                this.args('--apply', it.absolutePath)
            }
        } else {
            throw new IllegalArgumentException('No apply!')
        }

        if (this.data.getOrElse(false))
            this.args('--data')

        if (this.unpatched.getOrElse(false))
            this.args('--unpatched')
    }
}
