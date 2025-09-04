package net.minecraftforge.forgedev.tasks

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.forgedev.Util
import net.minecraftforge.gradleutils.shared.Tool
import net.minecraftforge.gradleutils.shared.ToolExecBase

import javax.inject.Inject

@CompileStatic
abstract class ToolExec extends ToolExecBase<ForgeDevProblems> implements ForgeDevTask {
    @Inject
    ToolExec(Tool tool) {
        super(ForgeDevProblems, tool)
    }

    @Override
    void exec() {
        // If the consumer hasn't manually set the command line arguments, add what we need.
        if (this.args.empty)
            this.addArguments()

        this.logger.info('{} {}', this.classpath.asPath, String.join(' ', this.args))

        super.exec()
    }
}
