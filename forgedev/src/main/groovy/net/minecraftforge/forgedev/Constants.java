/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.forgedev;

/// The package-private constants used throughout ForgeGradle.
public final class Constants {
    public static final String FORGE_MAVEN = "https://maven.minecraftforge.net/";

    public static final String MCMAVEN_VERSION = "0.3.1";
    public static final String MCMAVEN_DL_URL = "https://maven.minecraftforge.net/net/minecraftforge/minecraft-mavenizer/" + MCMAVEN_VERSION + "/minecraft-mavenizer-" + MCMAVEN_VERSION + ".jar";
    public static final String MCMAVEN_MAIN = "net.minecraftforge.mcmaven.cli.Main";
    public static final int MCMAVEN_JAVA_VERSION = 21;

    public static final String DIFFPATCH_VERSION = "2.0.0.36";
    public static final String DIFFPATCH_DL_URL = "https://repo1.maven.org/maven2/io/codechicken/DiffPatch/" + DIFFPATCH_VERSION + "/DiffPatch-" + DIFFPATCH_VERSION + "-all.jar";
    public static final String DIFFPATCH_MAIN = "io.codechicken.diffpatch.cli.DiffPatchCli";
    public static final int DIFFPATCH_JAVA_VERSION = 8;
}
