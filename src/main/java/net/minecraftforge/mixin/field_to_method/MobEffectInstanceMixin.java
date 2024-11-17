package net.minecraftforge.mixin.field_to_method;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffectInstance.class)
public class MobEffectInstanceMixin {
    /* this.effect -> this.getEffect() */

    @Redirect(
        method = {
            "<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V",
            "getParticleOptions()Lnet/minecraft/core/particles/ParticleOptions;",
            "update(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            "tick(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z",
            "onEffectStarted(Lnet/minecraft/world/entity/LivingEntity;)V",
            "onMobRemoved(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
            "onMobHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)V",
            "getDescriptionId()Ljava/lang/String;",
            "equals(Ljava/lang/Object;)Z",
            "hashCode()I",
            "onEffectAdded(Lnet/minecraft/world/entity/LivingEntity;)V",
            "is(Lnet/minecraft/core/Holder;)Z"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/effect/MobEffectInstance.effect:Lnet/minecraft/core/Holder;"
        )
    )
    private Holder<MobEffect> effect(MobEffectInstance effect) {
        return effect.getEffect();
    }
}
