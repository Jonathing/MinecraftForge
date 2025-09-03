package net.minecraftforge.forgedev;

import net.minecraftforge.gradleutils.shared.Tool;
import net.minecraftforge.util.download.DownloadUtils;
import net.minecraftforge.util.hash.HashStore;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static net.minecraftforge.forgedev.ForgeDevPlugin.LOGGER;

public final class Tools {
    private Tools() { }

    // EXECUTABLE
    public static final Tool MAVENIZER = tool(Constants.MAVENIZER_NAME, Constants.MAVENIZER_VERSION, Constants.MAVENIZER_DL_URL, Constants.MAVENIZER_JAVA, Constants.MAVENIZER_MAIN);
    public static final Tool DIFFPATCH = tool(Constants.DIFFPATCH_NAME, Constants.DIFFPATCH_VERSION, Constants.DIFFPATCH_DL_URL, Constants.DIFFPATCH_JAVA, Constants.DIFFPATCH_MAIN);
    public static final Tool BINPATCH = tool(Constants.BINPATCH_NAME, Constants.BINPATCH_VERSION, Constants.BINPATCH_DL_URL, Constants.BINPATCH_JAVA, Constants.BINPATCH_MAIN);
    public static final Tool SRG2SRC = tool(Constants.SRG2SRC_NAME, Constants.SRG2SRC_VERSION, Constants.SRG2SRC_DL_URL, Constants.SRG2SRC_JAVA, Constants.SRG2SRC_MAIN);

    // LIBRARIES
    public static final Tool SRGUTILS = tool(Constants.SRGUTILS_NAME, Constants.SRGUTILS_VERSION, Constants.SRGUTILS_DL_URL, Constants.SRGUTILS_JAVA);
    public static final Tool FASTCSV = tool(Constants.FASTCSV_NAME, Constants.FASTCSV_VERSION, Constants.FASTCSV_DL_URL, Constants.FASTCSV_JAVA);

    private static Tool tool(String name, String version, String downloadUrl, int javaVersion) {
        return Tool.of(name, version, downloadUrl, javaVersion);
    }

    private static Tool tool(String name, String version, String downloadUrl, int javaVersion, String mainClass) {
        return Tool.of(name, version, downloadUrl, javaVersion, mainClass);
    }
}
