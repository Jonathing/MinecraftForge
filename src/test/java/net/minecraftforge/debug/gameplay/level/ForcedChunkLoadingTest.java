/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.gameplay.level;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkAccess;
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
        var random = RandomSource.create();
        var level = helper.getLevel();
        var chunkSource = level.getChunkSource();

        var attempts = helper.intFlag("attempts", 0);
        helper.addCleanup(passed -> {
            if (!passed && attempts.getInt() > 0)
                helper.say("Failed to find an unloaded far-away chunk after " + attempts.getInt() + " attempts", ChatFormatting.RED);
        });

        ChunkAccess chunk;
        do {
            attempts.increment();
            int x = random.nextInt(1, 10) * 10000;
            int z = random.nextInt(1, 10) * 10000;
            chunk = level.getChunk(helper.absolutePos(BlockPos.ZERO).offset(x, 0, z));
        } while (!chunkSource.isPositionTicking(chunk.getPos().toLong()));
        attempts.set(0);

        var pos = chunk.getPos();
        helper.say("Attempting to force far away chunk: " + pos, ChatFormatting.YELLOW);
        chunkSource.chunkMap.getDistanceManager().addRegionTicket(TicketType.FORCED, pos, ChunkLevel.byStatus(FullChunkStatus.FULL), pos, true);

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().shouldForceTicks(pos.toLong()), "Chunk is not ticketed as a forced loaded chunk");
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(pos.toLong()), "Forced chunk cannot tick entities");
            helper.assertTrue(chunkSource.chunkMap.getDistanceManager().inBlockTickingRange(pos.toLong()), "Forced chunk cannot tick blocks");
            helper.assertTrue(chunkSource.hasChunk(pos.x, pos.z), "Chunk is not loaded despite being ticketed as a force loaded chunk");
            helper.succeed();
        });
    }
}
