package net.minecraftforge.forgedev;

import groovy.lang.Closure;
import net.minecraftforge.forgedev.tasks.compat.LegacyMergeFilesTask;
import net.minecraftforge.forgedev.tasks.filtering.LegacyFilterNewJar;
import net.minecraftforge.forgedev.tasks.mappings.LegacyGenerateSRG;
import net.minecraftforge.forgedev.tasks.mcp.MavenizerMCPDataTask;
import net.minecraftforge.forgedev.tasks.mcp.MavenizerMCPSetup;
import net.minecraftforge.forgedev.tasks.mcp.MavenizerRawArtifact;
import net.minecraftforge.forgedev.tasks.mcp.MavenizerSyncMappings;
import net.minecraftforge.forgedev.tasks.obfuscation.LegacyReobfuscateJar;
import net.minecraftforge.forgedev.tasks.patching.binary.CreateBinPatches;
import net.minecraftforge.forgedev.tasks.patching.diff.ApplyPatches;
import net.minecraftforge.forgedev.tasks.patching.diff.BakePatches;
import net.minecraftforge.forgedev.tasks.patching.diff.GeneratePatches;
import net.minecraftforge.forgedev.tasks.srg2source.ApplyRangeMap;
import net.minecraftforge.forgedev.tasks.srg2source.ExtractRangeMap;
import net.minecraftforge.gradleutils.shared.Closures;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

@VisibleForTesting
public abstract class ForgeDevExtension {
    public static final String NAME = "forgedev";

    private final ForgeDevProblems problems = this.getObjects().newInstance(ForgeDevProblems.class);

    private final DirectoryProperty mavenizerRepo = this.getObjects().directoryProperty();

    protected abstract @Inject ObjectFactory getObjects();

    protected abstract @Inject ProviderFactory getProviders();

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

        var legacyPatcher = project.getExtensions().create("patcher", LegacyPatcherExtension.class);
        var legacyMcp = project.getExtensions().create("mcp", LegacyMCPExtension.class);
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
    }

    private void finish(Project project) {
        var legacyPatcher = project.getExtensions().getByType(LegacyPatcherExtension.class);
        var legacyMcp = project.getExtensions().getByType(LegacyMCPExtension.class);

        var main = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().named(SourceSet.MAIN_SOURCE_SET_NAME);

        var tasks = project.getTasks();
        var srgSourcesJar = tasks.named("legacySourcesJar", Jar.class);
        var genPatches = tasks.named("genPatches", GeneratePatches.class);

        // Add the patched source as a source dir during afterEvaluate, to not be overwritten by buildscripts
        main.configure(s -> s.getJava().srcDir(legacyPatcher.getPatchedSrc()));

        // Automatically create the patches folder if it does not exist
        if (legacyPatcher.getPatches().isPresent()) {
            try {
                Files.createDirectories(legacyPatcher.getPatches().get().getAsFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create patches folder", e);
            }
            srgSourcesJar.configure(task -> task.from(genPatches.flatMap(GeneratePatches::getOutput), copy -> copy.into("patches/")));
        }


        // Add mappings so that it can be used by reflection tools.
        // net.minecraft:mappings_CHANNEL:VERSION@zip
        var mappingsDependency = project.getDependencies().create(
            "net.minecraft:mappings_%s:%s@zip".formatted(legacyPatcher.getMappingChannel().get(), legacyPatcher.getMappingVersion().get())
        );
        Util.runFirst(project, tasks.register("syncMappingsMaven", MavenizerSyncMappings.class, task -> {
            // TODO [ForgeDev][ForgeGradle 7] Support old MCP mappings (non-official)
            task.getVersion().set(legacyPatcher.getMappingVersion());
        }));
        project.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, mappingsDependency);

        var setupMCP = project.getTasks().register("setupMCP", MavenizerMCPSetup.class, task -> {
            task.getPipeline().set(legacyMcp.getPipeline());
            task.getArtifact().set(legacyMcp.getConfig());
        });
        legacyPatcher.getCleanSrc().set(setupMCP.flatMap(MavenizerMCPSetup::getOutput));
        var applyPatches = tasks.named("applyPatches", ApplyPatches.class, task -> task.getInput().convention(legacyPatcher.getCleanSrc()));
        genPatches.configure(task -> task.getInput().convention(legacyPatcher.getCleanSrc()));

        var extractSrg = tasks.register("extractSrg", MavenizerMCPDataTask.class, task -> task.getArtifact().set(legacyMcp.getConfig()));
        var createMcp2Srg = tasks.named("createMcp2Srg", LegacyGenerateSRG.class, task -> task.getMcpSrgData().convention(extractSrg.flatMap(MavenizerMCPDataTask::getOutput)));

        // TODO Configure filterNew
        tasks.withType(LegacyGenerateSRG.class, task ->
            task.getMappingsZip().set(
                project.getConfigurations().detachedConfiguration(mappingsDependency).getSingleFile()
            ));

        var createMcp2Obf = tasks.named("createMcp2Obf", LegacyGenerateSRG.class, task -> task.getMcpSrgData().convention(createMcp2Srg.flatMap(LegacyGenerateSRG::getMcpSrgData)));
        var createSrg2Mcp = tasks.named("createSrg2Mcp", LegacyGenerateSRG.class, task -> task.getMcpSrgData().convention(createMcp2Srg.flatMap(LegacyGenerateSRG::getMcpSrgData)));

        var userdevJar = tasks.named("userdevJar", Jar.class);

        // TODO CLIENT EXTRA?
        if (!legacyPatcher.getAccessTransformers().isEmpty()) {
            var mergeATs = tasks.register("mergeATs", LegacyMergeFilesTask.class, task -> {
                task.getFilesToMerge().setFrom(legacyPatcher.getAccessTransformers());

                task.getOutput().set(project.getLayout().getBuildDirectory().file("legacy-forgedev/merged_ats.cfg"));
            });
            setupMCP.configure(task -> {
                task.dependsOn(mergeATs);
                task.getAccessTransformerConfig().set(mergeATs.flatMap(LegacyMergeFilesTask::getOutput));
            });
            for (var f : legacyPatcher.getAccessTransformers()) {
                userdevJar.configure(t -> t.from(f, e -> e.into("ats/")));
                //userdevConfig.configure(t -> t.getATs().from(f));
            }
        }

        // TODO SAS! Used MCPFunction in FG6, I DON'T GIVE A SHIT RIGHT NOW!!!

        if (!legacyPatcher.getExtraMappings().isEmpty()) {
            for (var extraMapping : legacyPatcher.getExtraMappings()) {
                if (extraMapping instanceof File e) {
                    userdevJar.configure(t -> t.from(e, c -> c.into("srgs/")));
                    //userdevConfig.configure(t -> t.getSRGs().from(e));
                } else if (extraMapping instanceof String e) {
                    //userdevConfig.configure(t -> t.getSRGLines().add(e));
                }
            }
        }

        /*
        //UserDev Config Default Values
        userdevConfig.configure(task -> {
            task.getTool().convention("net.minecraftforge:binarypatcher:" + Constants.BINPATCH_VERSION + ":fatjar");
            task.getArguments().addAll("--clean", "{clean}", "--output", "{output}", "--apply", "{patch}");
            task.getUniversal().convention(universalJar.flatMap(t ->
                t.getArchiveBaseName().flatMap(baseName ->
                    t.getArchiveClassifier().flatMap(classifier ->
                        t.getArchiveExtension().map(jarExt ->
                            project.getGroup().toString() + ':' + baseName + ':' + project.getVersion() + ':' + classifier + '@' + jarExt
                        )))));
            task.getSource().convention(sourcesJar.flatMap(t ->
                t.getArchiveBaseName().flatMap(baseName ->
                    t.getArchiveClassifier().flatMap(classifier ->
                        t.getArchiveExtension().map(jarExt ->
                            project.getGroup().toString() + ':' + baseName + ':' + project.getVersion() + ':' + classifier + '@' + jarExt
                        )))));
            task.getPatchesOriginalPrefix().convention(genPatches.flatMap(GeneratePatches::getOriginalPrefix));
            task.getPatchesModifiedPrefix().convention(genPatches.flatMap(GeneratePatches::getModifiedPrefix));
            task.setNotchObf(extension.getNotchObf());
        });
         */

        var applyRangeMapBase = tasks.named("applyRangeMapBase", ApplyRangeMap.class);

        if (legacyPatcher.isSrgPatches()) {
            genPatches.configure(task -> task.getModified().set(applyRangeMapBase.flatMap(ApplyRangeMap::getOutput)));
        } else {
            var dirtyZip = tasks.register("patchedZip", Zip.class, task -> {
                task.from(legacyPatcher.getPatchedSrc());
                task.getArchiveFileName().set("output.zip");
                task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir(task.getName()));
            });

            // Fixup the inputs.
            applyPatches.configure(task -> {
                //task.getInput().set(toMCPClean.flatMap(LegacyApplyMappings::getOutput));
                //task.getInput().fileProvider(applyPatches.flatMap(a -> a.getInput().getAsFile()));
                task.getArchiveBase().set("zip");
            });
            genPatches.configure(task -> {
                //task.getInput().set(toMCPClean.flatMap(LegacyApplyMappings::getOutput));
                task.getInput().fileProvider(applyPatches.flatMap(a -> a.getInput().getAsFile()));
                task.getModified().set(dirtyZip.flatMap(AbstractArchiveTask::getArchiveFile));
            });

            // don't remember why this is blocked off, but it was in FG6 so i'm keeping it in here for now as well
            {
                var mcpConfigArtifact = legacyMcp.getConfig();
                var srgNames = this.getProviders().provider(() -> !legacyPatcher.getNotchObf());

                Function<String, TaskProvider<MavenizerRawArtifact>> rawJarTask = pipeline -> MavenizerRawArtifact.register(project, pipeline, mcpConfigArtifact, srgNames);
                var rawJoinedJar = rawJarTask.apply("joined");
                var rawClientJar = rawJarTask.apply("client");
                var rawServerJar = rawJarTask.apply("server");

                var srg = legacyPatcher.getNotchObf() ? createMcp2Obf : createMcp2Srg;
                var reobfJar = tasks.named("reobfJar", LegacyReobfuscateJar.class, task -> task.getSrg().set(srg.flatMap(LegacyGenerateSRG::getOutput)));

                var genJoinedBinPatches = tasks.named("genJoinedBinPatches", CreateBinPatches.class, task -> task.getClean().builtBy(rawJoinedJar));
                var genClientBinPatches = tasks.named("genClientBinPatches", CreateBinPatches.class, task -> task.getClean().builtBy(rawClientJar));
                var genServerBinPatches = tasks.named("genServerBinPatches", CreateBinPatches.class, task -> task.getClean().builtBy(rawServerJar));
                tasks.withType(CreateBinPatches.class, task -> {
                    task.getSrg().from(srg.flatMap(LegacyGenerateSRG::getOutput));
                    if (legacyPatcher.getPatches().isPresent()) {
                        task.mustRunAfter(genPatches);
                        task.getPatches().from(legacyPatcher.getPatches());
                    }
                });

                var filterNew = tasks.named("filterJarNew", LegacyFilterNewJar.class, task -> {
                    task.getSrg().set(srg.flatMap(LegacyGenerateSRG::getOutput));
                    task.getBlacklist().builtBy(rawJoinedJar);
                });
            }
        }
    }
}
