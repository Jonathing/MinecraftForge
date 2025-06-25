package net.minecraftforge.forgedev;

import net.minecraftforge.util.download.DownloadUtils;
import net.minecraftforge.util.hash.HashStore;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static net.minecraftforge.forgedev.ForgeDevPlugin.LOGGER;

public enum Tools {
    MAVENIZER("mavenizer-" + Constants.MAVENIZER_VERSION + ".jar", Constants.MAVENIZER_DL_URL),
    DIFFPATCH("diffpatch-" + Constants.DIFFPATCH_VERSION + ".jar", Constants.DIFFPATCH_DL_URL),
    BINPATCH("binpatcher-" + Constants.BINPATCH_VERSION + ".jar", Constants.BINPATCH_DL_URL);

    private final String fileName;
    private final String downloadUrl;

    Tools(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    /// Gets a provider for this tool using the given caches directory and provider factory.
    ///
    /// @param cachesDir The caches directory to store the tool
    /// @param providers The provider factory to use
    /// @return A provider for the tool as a [file][File]
    /// @deprecated Use [ForgeDevPlugin#getTool(Tools)] <- [org.gradle.api.plugins.PluginContainer#getPlugin(Class)]
    ///  <- [org.gradle.api.plugins.PluginAware#getPlugins()]
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    Provider<File> get(DirectoryProperty cachesDir, ProviderFactory providers) {
        return providers.of(Source.class, spec -> spec.parameters(parameters -> {
            parameters.getInputFile().set(cachesDir.file("tools/" + this.fileName));
            parameters.getDownloadUrl().set(this.downloadUrl);
        }));
    }

    static abstract class Source implements ValueSource<File, Source.Parameters> {
        interface Parameters extends ValueSourceParameters {
            @InputFile @PathSensitive(PathSensitivity.ABSOLUTE) RegularFileProperty getInputFile();

            @Input Property<String> getDownloadUrl();
        }

        @Inject
        public Source() { }

        @Override
        public File obtain() {
            Parameters parameters = this.getParameters();

            // inputs
            String downloadUrl = parameters.getDownloadUrl().get();

            // outputs
            File outFile = parameters.getInputFile().get().getAsFile();
            String name = outFile.getName();

            // in-house caching
            HashStore cache = HashStore.fromFile(outFile)
                                       .add("tool", outFile)
                                       .add("url", downloadUrl);

            if (outFile.exists() && cache.isSame()) {
                LOGGER.info("Default tool already downloaded: {}", name);
            } else {
                LOGGER.info("Downloading default tool: {}", name);
                try {
                    DownloadUtils.downloadFile(outFile, downloadUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to download default tool: " + name, e);
                }

                cache.add("tool", outFile).save();
            }

            return outFile;
        }
    }
}
