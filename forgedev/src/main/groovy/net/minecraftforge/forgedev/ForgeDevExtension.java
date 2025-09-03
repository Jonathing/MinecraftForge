package net.minecraftforge.forgedev;

import groovy.lang.Closure;
import net.minecraftforge.gradleutils.shared.Closures;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;

@VisibleForTesting
public abstract class ForgeDevExtension {
    public static final String NAME = "forgedev";

    private final ForgeDevPlugin plugin;
    private final ForgeDevProblems problems = this.getObjects().newInstance(ForgeDevProblems.class);

    private final DirectoryProperty mavenizerRepo = this.getObjects().directoryProperty();

    protected abstract @Inject ObjectFactory getObjects();

    @Inject
    public ForgeDevExtension(ForgeDevPlugin plugin) {
        this.plugin = plugin;
        this.mavenizerRepo.set(plugin.globalCaches().dir("repo").map(this.problems.ensureFileLocation()));
    }

    // NOTE: Pass into RepositoryHandler#maven
    @SuppressWarnings("rawtypes")
    public Closure getMaven() {
        return Closures.<MavenArtifactRepository>consumer(repo -> {
            repo.setName("ForgeDevMaven");
            repo.setUrl(this.mavenizerRepo);
        });
    }

    @VisibleForTesting
    public DirectoryProperty getMavenizerRepo() {
        return this.mavenizerRepo;
    }
}
