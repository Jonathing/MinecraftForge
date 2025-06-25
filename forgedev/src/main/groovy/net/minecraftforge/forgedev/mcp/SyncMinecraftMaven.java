/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.forgedev.mcp;

import net.minecraftforge.forgedev.Constants;
import net.minecraftforge.forgedev.ForgeDevProblems;
import net.minecraftforge.forgedev.ForgeDevTask;
import net.minecraftforge.forgedev.Tools;
import net.minecraftforge.forgedev.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.problems.Problems;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This task executes the Minecraft Mavenizer, Forge's standalone tool for generating a local Maven repository for
 * Minecraft artifacts.
 *
 * @see MinecraftExtensionImpl
 */
public abstract class SyncMinecraftMaven extends DefaultTask implements ForgeDevTask {
    /** The name of the task that is used to sync the Minecraft Maven. */
    static final String NAME = "syncMinecraftMaven";

    // TODO [ForgeDev][ForgeGradle] consolidate in FG7.
    //  For now, this syncs client/server/joined and the notch and search for all
    public static TaskProvider<SyncMinecraftMaven> register(Project project, Provider<? extends String> version) {
        return Util.runFirst(project, project.getTasks().register(NAME,
            SyncMinecraftMaven.class,
            task -> {
                String v = version.get();
                task.getRequests().addAll(Arrays.asList(
                    new Request("net.minecraft:client", v, "notch"),
                    new Request("net.minecraft:server", v, "notch"),
                    new Request("net.minecraft:joined", v, "notch"),
                    new Request("net.minecraft:client", v, "searge"),
                    new Request("net.minecraft:server", v, "searge"),
                    new Request("net.minecraft:joined", v, "searge")
                ));
            }
        ));
    }

    private final ForgeDevProblems problems;

    private final ExecOperations execOperations;

    @Inject
    public SyncMinecraftMaven(Problems problems, ObjectFactory objects, ProviderFactory providers, ExecOperations execOperations) {
        this.problems = new ForgeDevProblems(problems, providers);

        this.execOperations = execOperations;

        this.setGroup("Build Setup");
        this.setDescription("Syncs the Minecraft dependencies using Minecraft Mavenizer.");

        // JavaExec
        this.getExecutable().convention(this.getTool(Tools.MAVENIZER));
        this.getJavaLauncher().convention(Util.launcherForStrictly(this.getProject().getExtensions().getByType(JavaToolchainService.class), Constants.MAVENIZER_JAVA).map(j -> j.getExecutablePath().toString()));
        this.getMainClass().convention(Constants.MAVENIZER_MAIN);

        // Minecraft Maven
        DirectoryProperty defaultDirectory = objects.directoryProperty().value(this.getGlobalCaches().dir("mavenizer").map(this.problems.ensureFileLocation()));
        this.getCaches().convention(defaultDirectory.dir("cache").map(this.problems.ensureFileLocation()));
        this.getOutput().convention(defaultDirectory.dir("output").map(this.problems.ensureFileLocation()));

        /*
        this.onlyIf(
            "Minecraft Mavenizer will not run if no Minecraft dependencies are present.",
            task -> {
                var requests = ((SyncMinecraftMaven) task).getRequests();
                return requests.isPresent() && !requests.get().isEmpty();
            }
        );
         */
    }

    @TaskAction
    public void exec() {
        // TODO [ForgeGradle][MCMaven] Better logging for each request
        this.getRequests().get().forEach(this::exec);
    }

    private void exec(Request request) {
        this.execOperations.javaexec(spec -> {
            spec.setClasspath(this.getExecutable());
            spec.setExecutable(this.getJavaLauncher().get());
            spec.getMainClass().set(this.getMainClass());

            spec.setArgs(this.argsFor(request));
        }).rethrowFailure();
    }

    private List<String> argsFor(Request request) {
        List<String> args = new ArrayList<>(Arrays.asList(
            "--maven",
            "--cache", this.getCaches().get().getAsFile().getAbsolutePath(),
            "--output", this.getOutput().get().getAsFile().getAbsolutePath(),
            "--jdk-cache", this.getCaches().dir("jdks").get().getAsFile().getAbsolutePath(),
            "--artifact", request.module,
            "--version", request.version,
            "--mappings", request.mappingsChannel
        ));
        return args;
    }

    // JavaExec
    protected abstract @Classpath ConfigurableFileCollection getExecutable();

    protected abstract @Input Property<String> getJavaLauncher();

    protected abstract @Input Property<String> getMainClass();

    // Minecraft Mavenizer
    protected abstract @InputDirectory DirectoryProperty getCaches();

    protected abstract @InputDirectory DirectoryProperty getOutput();

    protected abstract @Input @Optional SetProperty<Request> getRequests();

    public static final class Request implements Serializable {
        private final String module;
        private final String version;
        private final String mappingsChannel;

        public Request(String module, String version, String mappingsChannel) {
            this.module = module;
            this.version = version;
            this.mappingsChannel = mappingsChannel;
        }
    }
}
