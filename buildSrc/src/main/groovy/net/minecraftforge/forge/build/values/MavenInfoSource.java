package net.minecraftforge.forge.build.values;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.inject.Inject;
import java.text.MessageFormat;

abstract class MavenInfoSource implements ValueSource<MavenInfo, MavenInfoSource.Parameters> {
    interface Parameters extends ValueSourceParameters {
        Property<String> getGroup();
        Property<String> getName();
        Property<String> getVersion();
        Property<String> getClassifier();
        Property<String> getExtension();
    }

    @Inject
    public MavenInfoSource() { }

    @Override
    public MavenInfo obtain() {
        var artGroup = getParameters().getGroup().get();
        var artName = getParameters().getName().get();
        var artVersion = getParameters().getVersion().get();
        var artClassifier = getParameters().getClassifier().getOrNull();
        var artExtension = getParameters().getExtension().getOrElse("jar");

        var key = artGroup + ':' + artName;
        var name = key + ':' + artVersion;
        var path = MessageFormat.format("{0}/{1}/{2}/{1}-{2}", artGroup.replace('.', '/'), artName, artVersion);
        if (artClassifier != null) {
            name += ':' + artClassifier;
            path += '-' + artClassifier;
        }
        if (!"jar".equals(artExtension)) {
            name += '@' + artExtension;
        }
        path += '.' + artExtension;

        return new MavenInfo(
            key,
            name,
            path,
            new MavenInfo.ArtifactInfo(
                artGroup,
                artName,
                artVersion,
                artClassifier,
                artExtension
            )
        );
    }
}
