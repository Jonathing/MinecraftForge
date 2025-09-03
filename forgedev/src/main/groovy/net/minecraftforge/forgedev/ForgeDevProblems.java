package net.minecraftforge.forgedev;

import groovy.transform.CompileStatic;
import net.minecraftforge.gradleutils.shared.EnhancedProblems;
import org.gradle.api.problems.Problems;

import javax.inject.Inject;
import java.io.Serial;

@CompileStatic
public abstract class ForgeDevProblems extends EnhancedProblems {
    private static final @Serial long serialVersionUID = 2518401307640491713L;

    @Inject
    public ForgeDevProblems() {
        super(ForgeDevPlugin.NAME, ForgeDevPlugin.DISPLAY_NAME);
    }
}
