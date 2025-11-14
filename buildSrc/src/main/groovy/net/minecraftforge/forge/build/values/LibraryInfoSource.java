package net.minecraftforge.forge.build.values;

import net.minecraftforge.forge.build.tasks.Util;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public abstract class LibraryInfoSource implements ValueSource<Map<String, LibraryInfo>, LibraryInfoSource.Parameters> {
    interface Parameters extends ValueSourceParameters {
        ListProperty<MinimalResolvedArtifact> getDependencies();
    }

    @Override
    public Map<String, LibraryInfo> obtain() {
        var dependencies = getParameters().getDependencies().get();

        var ret = new HashMap<String, LibraryInfo>(dependencies.size());
        var semaphore = new Semaphore(1, true);
        dependencies.parallelStream().forEachOrdered(dependency -> {
            var info = dependency.info();
            var url = "https://libraries.minecraft.net/" + info.path();
            if (!Util.checkExists(url))
                url = "https://maven.minecraftforge.net/" + info.path();

            var file = dependency.file();
            var sha1 = Util.sha1(dependency.file());

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to get library info for " + dependency.info(), e);
            }

            ret.put(info.key(), new LibraryInfo(
                info.name(),
                info.path(),
                url,
                sha1,
                file.length()
            ));

            semaphore.release();
        });

        return ret;
    }
}
