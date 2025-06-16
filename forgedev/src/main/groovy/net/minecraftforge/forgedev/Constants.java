/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.forgedev;

/// The package-private constants used throughout ForgeGradle.
public final class Constants {
    public static final String FORGE_MAVEN = "https://maven.minecraftforge.net/";
    public static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    public static final String MAVENIZER_VERSION = "0.3.3";
    public static final String MAVENIZER_DL_URL = FORGE_MAVEN + "net/minecraftforge/minecraft-mavenizer/" + MAVENIZER_VERSION + "/minecraft-mavenizer-" + MAVENIZER_VERSION + ".jar";
    public static final String MAVENIZER_MAIN = "net.minecraftforge.mcmaven.cli.Main";
    public static final int MAVENIZER_JAVA = 21;

    public static final String DIFFPATCH_VERSION = "2.0.0.36";
    public static final String DIFFPATCH_DL_URL = MAVEN_CENTRAL + "io/codechicken/DiffPatch/" + DIFFPATCH_VERSION + "/DiffPatch-" + DIFFPATCH_VERSION + "-all.jar";
    public static final String DIFFPATCH_MAIN = "io.codechicken.diffpatch.cli.DiffPatchCli";
    public static final int DIFFPATCH_JAVA = 8;
}
