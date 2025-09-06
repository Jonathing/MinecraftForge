package net.minecraftforge.forgedev.tasks

import groovy.transform.CompileStatic
import net.minecraftforge.forgedev.ForgeDevProblems
import net.minecraftforge.forgedev.ForgeDevTask
import net.minecraftforge.gradleutils.shared.Tool
import net.minecraftforge.gradleutils.shared.ToolExecBase

import javax.inject.Inject

@CompileStatic
abstract class ToolExec extends ToolExecBase<ForgeDevProblems> implements ForgeDevTask {
    @Inject
    ToolExec(Tool tool) {
        super(ForgeDevProblems, tool)
    }
}
