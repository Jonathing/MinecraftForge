package net.minecraftforge.forge.build;

import net.minecraftforge.gradleutils.shared.EnhancedProblems;

import javax.inject.Inject;
import java.io.Serial;

public abstract class ForgeBuildProblems extends EnhancedProblems {
    private static final @Serial long serialVersionUID = -34777297274844674L;

    @Inject
    public ForgeBuildProblems() {
        super(ForgeBuildPlugin.NAME, ForgeBuildPlugin.DISPLAY_NAME);
    }
}
