package net.minecraftforge.forge.build.values

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.minecraftforge.util.download.DownloadUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.jetbrains.annotations.Nullable

@CompileStatic
abstract class LatestForgeVersion implements ValueSource<String, Parameters> {
    private static final Logger LOGGER = Logging.getLogger(LatestForgeVersion)

    private static final String PROMOTIONS_SLIM = 'https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json'

    static interface Parameters extends ValueSourceParameters {
        Property<Boolean> getOffline();

        RegularFileProperty getPromotions();

        Property<String> getMinecraftVersion();
    }

    static Action<? super ValueSourceSpec<Parameters>> parameters(Project project, String minecraftVersion) {
        return { ValueSourceSpec<Parameters> spec ->
            spec.parameters { parameters ->
                parameters.offline.set(project.gradle.startParameter.offline)
                parameters.promotions.set(project.layout.buildDirectory.file('promotions_slim.json'))
                parameters.minecraftVersion.set(minecraftVersion)
            }
        }
    }

    @Override
    @Nullable String obtain() {
        try {
            final promotions = getParameters().getPromotions().getAsFile().get()

            // Always attempt to re-download promotions UNLESS we are offline
            if (getParameters().getOffline().getOrElse(false)) {
                if (!promotions.exists())
                    throw new IllegalStateException('Cannot download Forge promotions while offline! Please build the project at least once while online.')
            } else {
                DownloadUtils.downloadFile(promotions, PROMOTIONS_SLIM)
            }

            return parse(promotions)
        } catch (Exception e) {
            LOGGER.error('ERROR: Failed to get latest Forge version. Checks using this data will be skipped.', e)
            return null
        }
    }

    @CompileDynamic
    private String parse(File promotions) {
        final mcVersion = parameters.minecraftVersion.get()
        final json = new JsonSlurper().parseText(promotions.getText('UTF-8'))
        final ver = json.promos["$mcVersion-latest"]
        ver === null ? null : (mcVersion + '-' + ver)
    }
}
