package net.minecraftforge.forgedev.tasks.patching.diff

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import javax.inject.Inject

@CompileStatic
@PackageScope abstract class BasePatchBakingTask extends BaseDiffPatchExec {
    // Patch shared
    abstract @Input @Optional Property<String> getPrefix()

    @Inject
    BasePatchBakingTask(Problems problems) {
        super(problems)
    }

    @Override
    protected void addArguments() {
        super.addArguments()

        //region Patch shared
        if (this.prefix.present)
            this.args('--prefix', this.prefix.get())
        //endregion

        // https://github.com/TheCBProject/DiffPatch/blob/204d393ee23f5cd4298f771c7b9157ee21eb3b62/src/main/java/io/codechicken/diffpatch/cli/DiffPatchCli.java#L235
        // --bake {input_patches}
        this.args(
            '--bake',
            this.input.locationOnly.map(this.problems.ensureFileLocation()).get().asFile.absolutePath
        )
    }
}
