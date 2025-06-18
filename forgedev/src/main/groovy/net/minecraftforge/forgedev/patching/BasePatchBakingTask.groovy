package net.minecraftforge.forgedev.patching

import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import javax.inject.Inject

abstract class BasePatchBakingTask extends BaseDiffPatchExec {
    // Patch shared
    abstract @Input @Optional Property<String> getPrefix()

    @Inject
    BasePatchBakingTask(Problems problems) {
        super(problems)
    }

    @Override
    void exec() {
        //region Patch shared
        if (this.prefix.present)
            this.args('--prefix', this.prefix.get())
        //endregion

        this.args(
            '--bake'
        )

        super.exec()
    }
}
