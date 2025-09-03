package net.minecraftforge.forgedev;

import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;

public interface ForgeDevTask extends Task {
    @Internal
    default ForgeDevPlugin getForgedev() {
        return this.getProject().getPlugins().getPlugin(ForgeDevPlugin.class);
    }

    @Internal
    default Provider<Directory> getDefaultOutputDirectory() {
        return this.getForgedev().getLocalCaches().dir(this.getName());
    }

    @Internal
    default Provider<RegularFile> getDefaultOutputFile() {
        return this.getDefaultOutputFile("jar");
    }

    @Internal
    default Provider<RegularFile> getDefaultOutputFile(String ext) {
        return this.getForgedev().getLocalCaches().file("%s/output.%s".formatted(this.getName(), ext));
    }
}
