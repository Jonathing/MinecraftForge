package net.minecraftforge.forgedev;

import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

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
}
