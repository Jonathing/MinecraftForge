package net.minecraftforge.forgedev;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.Map;

@VisibleForTesting
public abstract class LegacyMinecraftExtension {
    protected final Project project;
    protected final ConfigurableFileCollection accessTransformers = this.getObjects().fileCollection();

    private final Property<String> mapping = this.getObjects().property(String.class);

    protected abstract @Inject ObjectFactory getObjects();

    @Inject
    public LegacyMinecraftExtension(Project project) {
        this.project = project;
        this.mapping.set(getMappingChannel().zip(getMappingVersion(), (ch, ver) -> ch + '_' + ver));
    }

    public Project getProject() {
        return project;
    }


    public abstract Property<String> getMappingChannel();

    public abstract Property<String> getMappingVersion();

    public Provider<String> getMappings() {
        return mapping;
    }

    public void mappings(Provider<String> channel, Provider<String> version) {
        getMappingChannel().set(channel);
        getMappingVersion().set(version);
    }

    public void mappings(String channel, String version) {
        getMappingChannel().set(channel);
        getMappingVersion().set(version);
    }

    public void mappings(Map<String, ? extends CharSequence> mappings) {
        CharSequence channel = mappings.get("channel");
        CharSequence version = mappings.get("version");

        if (channel == null || version == null) {
            throw new IllegalArgumentException("Must specify both mappings channel and version");
        }

        mappings(channel.toString(), version.toString());
    }

    public ConfigurableFileCollection getAccessTransformers() {
        return accessTransformers;
    }

    // Following are helper methods for adding ATs
    // Equivalents are not added for SASs as it's not used outside of Forge

    public void setAccessTransformers(Object... files) {
        getAccessTransformers().setFrom(files);
    }

    public void setAccessTransformer(Object files) {
        getAccessTransformers().setFrom(files);
    }

    public void accessTransformers(Object... files) {
        getAccessTransformers().from(files);
    }

    public void accessTransformer(Object file) {
        getAccessTransformers().from(file);
    }

    public abstract ConfigurableFileCollection getSideAnnotationStrippers();

    /**
     * If the Eclipse configurations should run the {@code prepareX} task before starting the game.
     * <p>
     * Default: {@code false}
     */
    public abstract Property<Boolean> getEnableEclipsePrepareRuns();

    /**
     * If the IntelliJ IDEA configurations should run the {@code prepareX} task before starting the game.
     * <p>
     * Default: {@code false}
     */
    public abstract Property<Boolean> getEnableIdeaPrepareRuns();

    /**
     * If Gradle resources should be copied to the respective IDE output folders before starting the game.
     * <p>
     * Default: {@code false}
     */
    public abstract Property<Boolean> getCopyIdeResources();

    /**
     * If run configurations should be grouped in folders.
     * <p>
     * Default: {@code false}
     */
    public abstract Property<Boolean> getGenerateRunFolders();
}
