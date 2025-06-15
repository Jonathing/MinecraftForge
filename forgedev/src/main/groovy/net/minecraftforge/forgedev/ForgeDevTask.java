package net.minecraftforge.forgedev;

import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;

import java.io.File;

public interface ForgeDevTask extends Task {
    @Internal
    default ForgeDevPlugin getPlugin() {
        return this.getProject().getPlugins().getPlugin(ForgeDevPlugin.class);
    }

    @Internal
    default Provider<File> getTool(Tools tool) {
        return this.getPlugin().getTool(tool);
    }

    @Internal
    default DirectoryProperty getGlobalCaches() {
        return this.getPlugin().getGlobalCaches();
    }
}
