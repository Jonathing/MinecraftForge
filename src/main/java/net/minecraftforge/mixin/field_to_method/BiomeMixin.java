package net.minecraftforge.mixin.field_to_method;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public class BiomeMixin {
    /* this.climateSettings -> this.getModifiedClimateSettings() */

    @Redirect(
        method = {
            "hasPrecipitation()Z",
            "getHeightAdjustedTemperature(Lnet/minecraft/core/BlockPos;I)F",
            "getGrassColorFromTexture()I",
            "getFoliageColorFromTexture()I",
            "getBaseTemperature()F"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/biome/Biome.climateSettings:Lnet/minecraft/world/level/biome/Biome$ClimateSettings;"
        )
    )
    private Biome.ClimateSettings climateSettings(Biome biome) {
        return biome.getModifiedClimateSettings();
    }

    @Redirect(
        method = {
            "lambda$static$5(Lnet/minecraft/world/level/biome/Biome;)Lnet/minecraft/world/level/biome/Biome$ClimateSettings;"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/biome/Biome.climateSettings:Lnet/minecraft/world/level/biome/Biome$ClimateSettings;"
        )
    )
    private static Biome.ClimateSettings climateSettings$static(Biome biome) {
        return biome.getModifiedClimateSettings();
    }


    /* this.specialEffects -> this.getModifiedSpecialEffects() */

    @Redirect(
        method = {
            "getSkyColor()I",
            "getFogColor()I",
            "getGrassColor(DD)I",
            "getFoliageColor()I",
            "getWaterColor()I",
            "getWaterFogColor()I",
            "getAmbientParticle()Ljava/util/Optional;",
            "getAmbientLoop()Ljava/util/Optional;",
            "getAmbientMood()Ljava/util/Optional;",
            "getAmbientAdditions()Ljava/util/Optional;",
            "getBackgroundMusic()Ljava/util/Optional;"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/biome/Biome.specialEffects:Lnet/minecraft/world/level/biome/BiomeSpecialEffects;"
        )
    )
    private BiomeSpecialEffects specialEffects(Biome biome) {
        return biome.getModifiedSpecialEffects();
    }

    @Redirect(
        method = {
            "lambda$static$6(Lnet/minecraft/world/level/biome/Biome;)Lnet/minecraft/world/level/biome/BiomeSpecialEffects;"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/biome/Biome.specialEffects:Lnet/minecraft/world/level/biome/BiomeSpecialEffects;"
        )
    )
    private static BiomeSpecialEffects specialEffects$static(Biome biome) {
        return biome.getModifiedSpecialEffects();
    }
}
