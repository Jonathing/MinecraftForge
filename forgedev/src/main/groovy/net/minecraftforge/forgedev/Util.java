package net.minecraftforge.forgedev;

import groovy.util.GroovyCollections;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.TaskExecutionRequest;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Util {
    /// @see #launcherFor(JavaPluginExtension, JavaToolchainService, JavaLanguageVersion)
    public static Provider<JavaLauncher> launcherFor(JavaPluginExtension java, JavaToolchainService javaToolchains, int version) {
        return launcherFor(java, javaToolchains, JavaLanguageVersion.of(version));
    }

    /// Gets the Java launcher that [can compile or run][JavaLanguageVersion#canCompileOrRun(JavaLanguageVersion)] the
    /// given version.
    ///
    /// If the currently running Java toolchain is able to compile and run the given version, it will be used instead.
    ///
    /// @param java           The Java plugin extension of the currently-used toolchain
    /// @param javaToolchains The Java toolchain service to get the Java launcher from
    /// @param version        The version of Java required
    /// @return A provider for the Java launcher
    public static Provider<JavaLauncher> launcherFor(JavaPluginExtension java, JavaToolchainService javaToolchains, JavaLanguageVersion version) {
        JavaToolchainSpec currentToolchain = java.getToolchain();
        return currentToolchain.getLanguageVersion().getOrElse(JavaLanguageVersion.current()).canCompileOrRun(version)
            ? javaToolchains.launcherFor(currentToolchain)
            : launcherForStrictly(javaToolchains, version);
    }

    /// @see #launcherForStrictly(JavaToolchainService, JavaLanguageVersion)
    public static Provider<JavaLauncher> launcherForStrictly(JavaToolchainService javaToolchains, int version) {
        return launcherForStrictly(javaToolchains, JavaLanguageVersion.of(version));
    }

    /// Gets the Java launcher strictly for the given version, even if the currently running Java toolchain is higher
    /// than it.
    ///
    /// @param javaToolchains The Java toolchain service to get the Java launcher from
    /// @param version        The version of Java required
    /// @return A provider for the Java launcher
    public static Provider<JavaLauncher> launcherForStrictly(JavaToolchainService javaToolchains, JavaLanguageVersion version) {
        return javaToolchains.launcherFor(spec -> spec.getLanguageVersion().set(version));
    }

    public static boolean isTrue(Provider<? extends String> provider) {
        return provider.map(Boolean::parseBoolean).getOrElse(false);
    }

    public static <T> T tryElse(Callable<? extends T> value, T orElse) {
        try {
            return Objects.requireNonNull(value.call());
        } catch (Throwable e) {
            return orElse;
        }
    }

    /// Ensures that a given task is run first in the task graph for the given project.
    ///
    /// This *does not* break the configuration cache as long as the task is always applied using this.
    ///
    /// @param project The project
    /// @param task    The task to run first
    public static <T extends TaskProvider<?>> T runFirst(Project project, T task) {
        // copy the requests because the backed list isn't concurrent
        ArrayList<TaskExecutionRequest> requests = new ArrayList<>(project.getGradle().getStartParameter().getTaskRequests());

        // add the task to the front of the list
        requests.add(0, new TaskExecutionRequest() {
            @Override
            public List<String> getArgs() {
                return Collections.singletonList(task.get().getPath());
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
