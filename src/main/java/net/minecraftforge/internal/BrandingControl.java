/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ObjIntConsumer;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;

/**
 * Controls the title screen brandings for the game.
 */
public final class BrandingControl {
    private BrandingControl() {}

    private static List<String> brandings;
    private static List<String> brandingsNoMC;

    private static void computeBranding() {
        if (brandings == null) {
            var list = new ArrayList<String>();

            // always show these ones
            list.add("Minecraft " + MCPVersion.getMCVersion());
            list.add("Forge " + ForgeVersion.getVersion() + " (" + ForgeI18n.parseMessage("fml.menu.loadingmods", ModList.get().size()) + ")");

            // additional debug versions
            if (ForgeConfig.CLIENT.debugBrandingVersions.get()) {
                // TODO [Forge][FML] When FML is rewritten, add its version here.
                list.add("MCP " + MCPVersion.getMCPVersion());
            }

            brandings = List.copyOf(list);
            brandingsNoMC = brandings.subList(1, brandings.size());
        }
    }

    private static List<String> getBrandings(boolean includeMC, boolean reverse) {
        computeBranding();
        if (includeMC)
            return reverse ? brandings.reversed() : brandings;
        else
            return reverse ? brandingsNoMC.reversed() : brandingsNoMC;
    }

    /**
     * Gets the branding lines to display over the copyright line. This is usually a message when Forge has an update.
     *
     * @return The branding lines to display
     */
    public static List<String> getOverCopyrightBrandings() {
        final class LazyInit {
            private static final List<String> INSTANCE = ForgeHooksClient.forgeStatusLine == null
                    ? Collections.emptyList()
                    : List.of(ForgeHooksClient.forgeStatusLine);

            private LazyInit() {}
        }

        return LazyInit.INSTANCE;
    }

    /**
     * Iterates over each branding line, passing the line and its index to the consumer.
     *
     * @param includeMC    Whether to include the Minecraft version line
     * @param reverse      Whether to iterate in reverse order
     * @param lineConsumer The consumer to accept each line and its index
     */
    public static void forEachLine(boolean includeMC, boolean reverse, ObjIntConsumer<String> lineConsumer) {
        var brandings = getBrandings(includeMC, reverse);
        for (int idx = 0; idx < brandings.size(); idx++)
            lineConsumer.accept(brandings.get(idx), idx);
    }

    /**
     * Iterates over each branding line that should be displayed above the copyright line, passing the line and its
     * index to the consumer.
     *
     * @param lineConsumer The consumer to accept each line and its index
     */
    public static void forEachAboveCopyrightLine(ObjIntConsumer<String> lineConsumer) {
        var overCopyrightBrandings = getOverCopyrightBrandings();
        for (int idx = 0; idx < overCopyrightBrandings.size(); idx++)
            lineConsumer.accept(overCopyrightBrandings.get(idx), idx);
    }

    /**
     * Gets the branding to use in place of the default {@code "vanilla"} branding.
     *
     * @return The branding to use
     */
    public static String getBranding() {
        return "forge";
    }

    /**
     * The reload listener for the branding control. On reload, the brandings are recomputed in
     * {@link #computeBranding()}.
     *
     * @return The reload listener
     */
    public static ResourceManagerReloadListener resourceManagerReloadListener() {
        return BrandingControl::onResourceManagerReload;
    }

    private static void onResourceManagerReload(ResourceManager resourceManager) {
        brandings = null;
        brandingsNoMC = null;
    }
}
