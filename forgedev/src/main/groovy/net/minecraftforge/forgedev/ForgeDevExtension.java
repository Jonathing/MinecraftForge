package net.minecraftforge.forgedev;

import groovy.lang.Closure;
import net.minecraftforge.forgedev.tasks.filtering.LegacyFilterNewJar;
import net.minecraftforge.forgedev.tasks.mappings.LegacyGenerateSRG;
import net.minecraftforge.forgedev.tasks.obfuscation.LegacyReobfuscateJar;
import net.minecraftforge.forgedev.tasks.patching.binary.CreateBinPatches;
import net.minecraftforge.forgedev.tasks.patching.diff.ApplyPatches;
import net.minecraftforge.forgedev.tasks.patching.diff.BakePatches;
import net.minecraftforge.forgedev.tasks.patching.diff.GeneratePatches;
import net.minecraftforge.forgedev.tasks.srg2source.ApplyRangeMap;
import net.minecraftforge.forgedev.tasks.srg2source.ExtractRangeMap;
import net.minecraftforge.gradleutils.shared.Closures;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

@VisibleForTesting
public abstract class ForgeDevExtension {
    public static final String NAME = "forgedev";

    private final ForgeDevProblems problems = this.getObjects().newInstance(ForgeDevProblems.class);

    private final DirectoryProperty mavenizerRepo = this.getObjects().directoryProperty();

    protected abstract @Inject ObjectFactory getObjects();

    @Inject
    public ForgeDevExtension(ForgeDevPlugin plugin, Project project) {
        this.mavenizerRepo.set(plugin.globalCaches().dir("repo").map(this.problems.ensureFileLocation()));
    }

    // NOTE: Pass into RepositoryHandler#maven
    @SuppressWarnings("rawtypes")
    public Closure getMaven() {
        return Closures.<MavenArtifactRepository>consumer(repo -> {
            repo.setName("ForgeDevMaven");
            repo.setUrl(this.mavenizerRepo);
        });
    }

    @VisibleForTesting
    public DirectoryProperty getMavenizerRepo() {
        return this.mavenizerRepo;
    }

    private void setup(ForgeDevPlugin plugin, Project project) {
        var tasks = project.getTasks();

        var legacyPatcher = project.getExtensions().create("legacyPatcher", LegacyPatcherExtension.class);
        var java = project.getExtensions().getByType(JavaPluginExtension.class);

        var jar = tasks.named(JavaPlugin.JAR_TASK_NAME, Jar.class);
        var compileJava = tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
        var main = java.getSourceSets().named(SourceSet.MAIN_SOURCE_SET_NAME);

        var applyPatches = tasks.register("applyPatches", ApplyPatches.class, task -> {
            final Provider<Directory> workDir = project.getLayout().getBuildDirectory().dir(task.getName());
            task.getOutput().set(workDir.map(s -> s.file("output.zip")));
            task.getArchive().set("zip");
            task.setRejects(workDir.map(s -> s.file("rejects.zip")));
            task.getArchiveRejects().set("zip");
            task.getPatches().set(legacyPatcher.getPatches());
            task.getMode().set("access");
            if (project.hasProperty("UPDATING")) {
                task.getMode().set("fuzzy");
                task.setRejects(project.getLayout().getProjectDirectory().dir("rejects"));
                task.getArchiveRejects().unset();
                task.getFailOnError().set(false);
            }
        });

        var extractRangeMap = tasks.register("extractRangeMap", ExtractRangeMap.class, task -> {
            task.getDependencies().from(jar.flatMap(Jar::getArchiveFile));

            // Only add main source, as we inject the patchedSrc into it as a sourceset.
            task.getSources().from(main.map(s -> s.getJava().getSourceDirectories()));
            task.getDependencies().from(compileJava.map(JavaCompile::getClasspath));
        });

        var createMcp2Srg = tasks.register("createMcp2Srg", LegacyGenerateSRG.class, task -> task.getReverse().set(true));
        var createSrg2Mcp = tasks.register("createSrg2Mcp", LegacyGenerateSRG.class, task -> task.getReverse().set(false));
        var createMcp2Obf = tasks.register("createMcp2Obf", LegacyGenerateSRG.class, task -> {
            task.getNotch().set(true);
            task.getReverse().set(true);
        });

        // TODO DOES NOTHING!
        var createExc = tasks.register("createExc");

        var applyRangeMap = tasks.register("applyRangeMap", ApplyRangeMap.class, task -> {
            task.getSources().from(main.map(s -> s.getJava().getSourceDirectories().minus(project.files(legacyPatcher.getPatchedSrc()))));
            task.setOnlyIf(t -> !((ApplyRangeMap) t).getSources().isEmpty());
            task.getRangeMap().set(extractRangeMap.flatMap(ExtractRangeMap::getOutput));
            task.getSrgFiles().from(createMcp2Srg.flatMap(LegacyGenerateSRG::getOutput));
            task.getExcFiles().from(/*createExc.flatMap(CreateExc::getOutput), */legacyPatcher.getExcs());
        });

        var applyRangeMapBase = tasks.register("applyRangeMapBase", ApplyRangeMap.class, task -> {
            task.setOnlyIf(t -> legacyPatcher.getPatches().isPresent());
            task.getSources().from(legacyPatcher.getPatchedSrc());
            task.getRangeMap().set(extractRangeMap.flatMap(ExtractRangeMap::getOutput));
            task.getSrgFiles().from(createMcp2Srg.flatMap(LegacyGenerateSRG::getOutput));
            task.getExcFiles().from(/*createExc.flatMap(CreateExc::getOutput), */legacyPatcher.getExcs());
        });

        var genPatches = tasks.register("genPatches", GeneratePatches.class, task -> {
            task.setOnlyIf(t -> legacyPatcher.getPatches().isPresent());
            task.getOutput().set(legacyPatcher.getPatches());
        });

        var bakePatches = tasks.register("bakePatches", BakePatches.class, task -> {
            task.dependsOn(genPatches);
            task.getInput().set(legacyPatcher.getPatches());
            task.getOutput().set(new File(task.getTemporaryDir(), "output.zip"));
        });

        var reobfJar = tasks.register("reobfJar", LegacyReobfuscateJar.class, task -> {
            task.getInput().set(jar.flatMap(Jar::getArchiveFile));
            // TODO Optimize this to use a detached configuraiton
            task.getLibraries().from(project.getConfigurations().named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME));
        });

        var genJoinedBinPatches = tasks.register("genJoinedBinPatches", CreateBinPatches.class, task -> {
            task.getCreate().from(reobfJar.flatMap(LegacyReobfuscateJar::getOutput));
            task.getOutput().convention(project.getLayout().getBuildDirectory().dir(task.getName()).map(d -> d.file("joined.lzma")));
        });
        var genClientBinPatches = tasks.register("genClientBinPatches", CreateBinPatches.class, task -> {
            task.getCreate().from(reobfJar.flatMap(LegacyReobfuscateJar::getOutput));
            task.getOutput().convention(project.getLayout().getBuildDirectory().dir(task.getName()).map(d -> d.file("client.lzma")));
        });
        var genServerBinPatches = tasks.register("genServerBinPatches", CreateBinPatches.class, task -> {
            task.getCreate().from(reobfJar.flatMap(LegacyReobfuscateJar::getOutput));
            task.getOutput().convention(project.getLayout().getBuildDirectory().dir(task.getName()).map(d -> d.file("server.lzma")));
        });
        var genBinPatches = tasks.register("genBinPatches", task -> task.dependsOn(genJoinedBinPatches, genClientBinPatches, genServerBinPatches));

        var filterNew = tasks.register("filterJarNew", LegacyFilterNewJar.class, task -> task.getInput().set(reobfJar.flatMap(LegacyReobfuscateJar::getOutput)));

        /*
         * All sources in SRG names.
         * patches in /patches/
         */
        // TODO This may conflict with normal sources jar if enabled
        //      Remember that we want to generalize ForgeDev to be used by both Forge and ForgeLoader
        var srgSourcesJar = tasks.register("legacySourcesJar", Jar.class, task -> {
            task.setOnlyIf(t -> applyRangeMap.flatMap(ApplyRangeMap::getOutput).map(rf -> rf.getAsFile().exists()).getOrElse(false));
            task.dependsOn(applyRangeMap);
            task.from(project.zipTree(applyRangeMap.flatMap(ApplyRangeMap::getOutput)));
            task.getArchiveClassifier().set("sources");
        });

        /* Universal:
         * All of our classes and resources as normal jar.
         *   Should only be OUR classes, not parent patcher projects.
         */
        var universalJar = tasks.register("universalJar", Jar.class, task -> {
            task.dependsOn(filterNew);
            task.from(project.zipTree(filterNew.flatMap(LegacyFilterNewJar::getOutput)));
            task.from(java.getSourceSets().named(SourceSet.MAIN_SOURCE_SET_NAME).map(SourceSet::getResources));
            task.getArchiveClassifier().set("universal");
        });

        /*UserDev:
         * config.json
         * joined.lzma
         * sources.jar
         * patches/
         *   net/minecraft/item/Item.java.patch
         * ats/
         *   at1.cfg
         *   at2.cfg
         */
        var userdevJar = tasks.register("userdevJar", Jar.class, task -> {
            task.dependsOn(srgSourcesJar, bakePatches);
            task.setOnlyIf(t -> legacyPatcher.isSrgPatches());
            // TODO WE NEED USERDEV CONFIG!!! See GenerateUserdevConfig in FG6
            //task.from(userdevConfig.flatMap(GenerateUserdevConfig::getOutput), e -> e.rename(f -> "config.json"));
            task.from(genJoinedBinPatches.flatMap(CreateBinPatches::getOutput), e -> e.rename(f -> "joined.lzma"));
            task.from(project.zipTree(bakePatches.flatMap(BakePatches::getOutput)), e -> e.into("patches/"));
            task.getArchiveClassifier().set("userdev");
        });
        var release = tasks.register("release", task -> task.dependsOn(srgSourcesJar, universalJar, userdevJar));

        // TODO THIS
        /*
        final boolean doingUpdate = project.hasProperty("UPDATE_MAPPINGS");
        final String updateVersion = doingUpdate ? (String) project.property("UPDATE_MAPPINGS") : null;
        final String updateChannel = doingUpdate
                ? (project.hasProperty("UPDATE_MAPPINGS_CHANNEL") ? (String) project.property("UPDATE_MAPPINGS_CHANNEL") : "snapshot")
                : null;
        if (doingUpdate) {
            TaskProvider<DownloadMCPMappings> dlMappingsNew = tasks.register("downloadMappingsNew", DownloadMCPMappings.class);
            dlMappingsNew.get().getMappings().set(updateChannel + '_' + updateVersion);

            TaskProvider<LegacyApplyMappings> toMCPNew = tasks.register("srg2mcpNew", LegacyApplyMappings.class);
            toMCPNew.configure(task -> {
                task.getInput().set(applyRangeConfig.flatMap(ApplyRangeMap::getOutput));
                task.getMappingsZip().set(dlMappingsConfig.flatMap(DownloadMCPMappings::getOutput));
                task.getLambdas().set(false);
            });

            TaskProvider<LegacyExtractExistingFiles> extractMappedNew = tasks.register("extractMappedNew", LegacyExtractExistingFiles.class);
            extractMappedNew.configure(task -> {
                task.getArchive().set(toMCPNew.flatMap(LegacyApplyMappings::getOutput));
                task.getTargets().from(mainSource.map(s -> s.getJava().getSourceDirectories().minus(project.files(extension.getPatchedSrc()))));
            });

            TaskProvider<DefaultTask> updateMappings = tasks.register("updateMappings", DefaultTask.class);
            updateMappings.configure(task -> task.dependsOn(extractMappedNew));
        }
         */

        // TODO split into its own method, private void finish(Project project)
        project.afterEvaluate(p -> {
            // Add the patched source as a source dir during afterEvaluate, to not be overwritten by buildscripts
            main.configure(s -> s.getJava().srcDir(legacyPatcher.getPatchedSrc()));

            // Automatically create the patches folder if it does not exist
            if (legacyPatcher.getPatches().isPresent()) {
                try {
                    Files.createDirectories(legacyPatcher.getPatches().get().getAsFile());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create patches folder", e);
                }
                srgSourcesJar.configure(task -> task.from(genPatches.flatMap(GeneratePatches::getOutput), copy -> copy.into("patches/")));
            }
        });
    }
}
