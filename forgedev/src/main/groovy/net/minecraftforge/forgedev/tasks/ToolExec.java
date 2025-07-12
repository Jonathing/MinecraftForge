package net.minecraftforge.forgedev.tasks;

import net.minecraftforge.forgedev.ForgeDevPlugin;
import net.minecraftforge.forgedev.ForgeDevProblems;
import net.minecraftforge.forgedev.ForgeDevTask;
import net.minecraftforge.forgedev.Tools;
import net.minecraftforge.forgedev.Util;
import org.gradle.api.problems.Problems;
import org.gradle.api.tasks.JavaExec;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import javax.inject.Inject;

public abstract class ToolExec extends JavaExec implements ForgeDevTask {
    protected final ForgeDevProblems problems;

    @Inject
    public ToolExec(Problems problems, Tools tool) {
        this.problems = new ForgeDevProblems(problems, this.getProviderFactory());

        this.setClasspath(this.getObjectFactory().fileCollection().from(this.getTool(tool)));
        this.getMainClass().convention(tool.mainClass);
        this.getJavaLauncher().convention(Util.launcherForStrictly(this.getJavaToolchainService(), tool.javaVersion));
    }

    @MustBeInvokedByOverriders
    protected abstract void addArguments();

    @Override
    @MustBeInvokedByOverriders
    public void exec() {
        // If the consumer hasn't manually set the command line arguments, add what we need.
        if (this.getArgs().isEmpty())
            this.addArguments();

        this.getLogger().info("{} {}", this.getClasspath().getAsPath(), String.join(" ", this.getArgs()));

        super.exec();
    }
}
