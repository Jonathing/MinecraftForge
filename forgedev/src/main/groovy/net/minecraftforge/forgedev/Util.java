package net.minecraftforge.forgedev;

import net.minecraftforge.gradleutils.shared.SharedUtil;
import org.gradle.TaskExecutionRequest;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public final class Util extends SharedUtil {
    private Util() { }

    /// Ensures that a given task is run first in the task graph for the given project.
    ///
    /// This *does not* break the configuration cache as long as the task is always applied using this.
    ///
    /// @param project The project
    /// @param task    The task to run first
    public static <T extends TaskProvider<?>> T runFirst(Project project, T task) {
        // copy the requests because the backed list isn't concurrent
        var requests = new ArrayList<>(project.getGradle().getStartParameter().getTaskRequests());

        // add the task to the front of the list
        requests.add(0, new TaskExecutionRequest() {
            @Override
            public List<String> getArgs() {
                return List.of(task.get().getPath());
            }

            @Override
            public @Nullable String getProjectPath() {
                return null;
            }

            @Override
            public @Nullable File getRootDir() {
                return null;
            }
        });

        // set the new requests
        project.getLogger().info("Adding task to beginning of task graph! Project: {}, Task: {}", project.getName(), task.getName());
        project.getGradle().getStartParameter().setTaskRequests(requests);
        return task;
    }

}
