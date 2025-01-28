/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.gameplay.level;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.TicketType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.test.BaseTestMod;

@GameTestHolder("forge." + ForcedChunkLoadingTest.MOD_ID)
@Mod(ForcedChunkLoadingTest.MOD_ID)
public class ForcedChunkLoadingTest extends BaseTestMod {
    static final String MOD_ID = "forced_chunk_loading";

    public ForcedChunkLoadingTest(FMLJavaModLoadingContext context) {
        super(context);
    }

    @GameTest(template = "forge:empty3x3x3")
    public static void force_far_away_chunk(GameTestHelper helper) {
        var level = helper.getLevel();
        var chunkSource = level.getChunkSource();

        var chunk = level.getChunk(new BlockPos(10000, 0, 10000));
        var pos = chunk.getPos();
        helper.say("Attempting to force far away chunk: " + pos, ChatFormatting.YELLOW);
        chunkSource.chunkMap.getDistanceManager().addRegionTicket(TicketType.FORCED, pos, ChunkLevel.byStatus(FullChunkStatus.FULL), pos, true);

        helper.runAfterDelay(90, () -> {
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().shouldForceTicks(pos.toLong()), "Chunk is not ticketed as a forced loaded chunk");
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(pos.toLong()), "Forced chunk cannot tick entities");
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().inBlockTickingRange(pos.toLong()), "Forced chunk cannot tick blocks");
            helper.succeed();
        });
    }
}
