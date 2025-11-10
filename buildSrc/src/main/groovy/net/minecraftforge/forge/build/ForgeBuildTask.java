package net.minecraftforge.forge.build;

import net.minecraftforge.gradleutils.shared.EnhancedPlugin;
import net.minecraftforge.gradleutils.shared.EnhancedTask;
import org.gradle.api.Project;

public interface ForgeBuildTask extends EnhancedTask<ForgeBuildProblems> {
    @Override
    default Class<? extends EnhancedPlugin<? super Project>> pluginType() {
        return ForgeBuildPlugin.class;
    }

    @Override
    default Class<ForgeBuildProblems> problemsType() {
        return ForgeBuildProblems.class;
    }
}
