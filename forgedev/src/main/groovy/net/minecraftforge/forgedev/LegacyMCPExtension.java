package net.minecraftforge.forgedev;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public abstract class LegacyMCPExtension {
    public static final String EXTENSION_NAME = "mcp";

    private final Property<String> config = this.getObjects().property(String.class);

    protected abstract @Inject ObjectFactory getObjects();

    protected abstract @Inject ProviderFactory getProviders();

    @Inject
    public LegacyMCPExtension(final Project project) { }

    public Property<String> getConfig() {
        return this.config;
    }

    public void setConfig(Provider<String> value) {
        getConfig().set(value.map(s -> {
            if (s.indexOf(':') != -1) { // Full artifact
                return s;
            } else {
                return "de.oceanlabs.mcp:mcp_config:" + s + "@zip";
            }
        }));
    }

    public void setConfig(String value) {
        setConfig(this.getProviders().provider(() -> value));
    }

    public abstract Property<String> getPipeline();
}
