package net.minecraftforge.forgedev;

import groovy.lang.Closure;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class ForgeDevExtension {
    public static final String NAME = "forgedev";

    private final ForgeDevPlugin plugin;

    public ForgeDevExtension(ForgeDevPlugin plugin) {
        this.plugin = plugin;
    }

    // NOTE: Pass into RepositoryHandler#maven
    @SuppressWarnings("rawtypes")
    public Closure getMaven() {
        return Closures.<MavenArtifactRepository>consumer(repo -> {
            repo.setName("ForgeDevMaven");
            repo.setUrl(this.plugin.getMavenizerRepo());
        });
    }
}
