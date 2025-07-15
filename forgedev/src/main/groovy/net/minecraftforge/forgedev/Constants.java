/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.forgedev;

/// The package-private constants used throughout ForgeGradle.
public final class Constants {
    public static final String FORGE_MAVEN = "https://maven.minecraftforge.net/";
    public static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    public static final String MAVENIZER_VERSION = "0.3.13";
    public static final String MAVENIZER_DL_URL = FORGE_MAVEN + "net/minecraftforge/minecraft-mavenizer/" + MAVENIZER_VERSION + "/minecraft-mavenizer-" + MAVENIZER_VERSION + ".jar";
    public static final String MAVENIZER_MAIN = "net.minecraftforge.mcmaven.cli.Main";
    public static final int MAVENIZER_JAVA = 21;

    public static final String DIFFPATCH_VERSION = "2.0.1.39";
    public static final String DIFFPATCH_DL_URL = MAVEN_CENTRAL + "io/codechicken/DiffPatch/" + DIFFPATCH_VERSION + "/DiffPatch-" + DIFFPATCH_VERSION + "-all.jar";
    public static final String DIFFPATCH_MAIN = "io.codechicken.diffpatch.cli.DiffPatchCli";
    public static final int DIFFPATCH_JAVA = 8;

    public static final String BINPATCH_VERSION = "1.2.2";
    public static final String BINPATCH_DL_URL = FORGE_MAVEN + "net/minecraftforge/binarypatcher/" + BINPATCH_VERSION + "/binarypatcher-" + BINPATCH_VERSION + "-fatjar.jar";
    public static final String BINPATCH_MAIN = "net.minecraftforge.binarypatcher.ConsoleTool";
    public static final int BINPATCH_JAVA = 8;

    public static final String SRGUTILS_VERSION = "0.5.14";
    public static final String SRGUTILS_DL_URL = FORGE_MAVEN + "net/minecraftforge/srgutils/" + SRGUTILS_VERSION + "/srgutils-" + SRGUTILS_VERSION + ".jar";
    public static final int SRGUTILS_JAVA = 8;

    public static final String FASTCSV_VERSION = "3.7.0";
    public static final String FASTCSV_DL_URL = MAVEN_CENTRAL + "de/siegmar/fastcsv/" + FASTCSV_VERSION + "/fastcsv-" + FASTCSV_VERSION + ".jar";
    public static final int FASTCSV_JAVA = 11;
}
