package net.minecraftforge.mixin.field_to_method;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowerPotBlock.class)
public class FlowerPotBlockMixin {
    /* this.potted -> this.getPotted() */

    @Redirect(
        method = {
            "useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            "getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            "isEmpty()Z"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/block/FlowerPotBlock.potted:Lnet/minecraft/world/level/block/Block;"
        )
    )
    private Block effect(FlowerPotBlock block) {
        return block.getPotted();
    }

    @Redirect(
        method = {
            "lambda$static$0(Lnet/minecraft/world/level/block/FlowerPotBlock;)Lnet/minecraft/world/level/block/Block;"
        },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "net/minecraft/world/level/block/FlowerPotBlock.potted:Lnet/minecraft/world/level/block/Block;"
        )
    )
    private static Block effect$static(FlowerPotBlock block) {
        return block.getPotted();
    }
}
