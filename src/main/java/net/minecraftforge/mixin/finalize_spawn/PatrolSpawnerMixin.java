package net.minecraftforge.mixin.finalize_spawn;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {
    @Redirect(
        method = "spawnPatrolMember(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;Z)Z",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/world/entity/monster/PatrollingMonster.finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"
        )
    )
    private SpawnGroupData finalizeSpawn(PatrollingMonster mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, SpawnGroupData spawnData) {
        return ForgeEventFactory.onFinalizeSpawn(mob, level, difficulty, spawnType, spawnData);
    }
}
