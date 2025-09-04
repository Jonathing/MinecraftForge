package net.minecraftforge.forgedev;

import net.minecraftforge.util.data.json.PatcherConfig;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@VisibleForTesting
public abstract class LegacyPatcherExtension extends LegacyMinecraftExtension {
    public static final String EXTENSION_NAME = "patcher";

    private boolean srgPatches = true;
    private boolean notchObf = false;

    private List<Object> extraExcs, extraMappings;

    @Nullable
    private PatcherConfig.V2.DataFunction processor;

    public LegacyPatcherExtension(final Project project) {
        super(project);
    }

    public abstract Property<Project> getParent();

    public abstract RegularFileProperty getCleanSrc();

    public abstract DirectoryProperty getPatchedSrc();

    public abstract DirectoryProperty getPatches();

    public abstract Property<String> getMcVersion();

    public boolean isSrgPatches() {
        return srgPatches;
    }

    public void setSrgPatches(boolean srgPatches) {
        this.srgPatches = srgPatches;
    }

    public boolean getNotchObf() {
        return this.notchObf;
    }

    public void setNotchObf(boolean value) {
        this.notchObf = value;
    }

    public abstract ConfigurableFileCollection getExcs();

    public void setExtraExcs(List<Object> extraExcs) {
        this.extraExcs = new ArrayList<>(extraExcs);
    }

    public void extraExcs(Object... excs) {
        getExtraExcs().addAll(Arrays.asList(excs)); // TODO: Type check!
    }

    public void extraExc(Object exc) {
        extraExcs(exc); // TODO: Type check!
    }

    public List<Object> getExtraExcs() {
        if (extraExcs == null) {
            extraExcs = new ArrayList<>();
        }

        return extraExcs;
    }

    public void extraMapping(Object mapping) {
        if (mapping instanceof String || mapping instanceof File) {
            getExtraMappings().add(mapping);
        } else {
            throw new IllegalArgumentException("Extra mappings must be a file or a string!");
        }
    }

    public void setExtraMappings(List<Object> extraMappings) {
        this.extraMappings = new ArrayList<>(extraMappings);
    }

    public List<Object> getExtraMappings() {
        if (extraMappings == null) {
            extraMappings = new ArrayList<>();
        }

        return extraMappings;
    }

    @Nullable
    public PatcherConfig.V2.DataFunction getProcessor() {
        return this.processor;
    }

    public abstract MapProperty<String, File> getProcessorData();

    void copyFrom(LegacyPatcherExtension other) {
        getMappingChannel().set(other.getMappingChannel());
        getMappingVersion().set(other.getMappingVersion());
        getMcVersion().set(other.getMcVersion());
    }
}
