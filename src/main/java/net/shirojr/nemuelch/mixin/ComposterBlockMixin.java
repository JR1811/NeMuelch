package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.Optional;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {
    @Inject(method = "emptyFullComposter", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setToDefaultPickupDelay()V"))
    private static void addBlight(Entity user, BlockState state, World world, BlockPos pos, CallbackInfoReturnable<BlockState> cir, @Local ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getStack().copy();
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(world.getChunk(pos));
        if (blightChunkComponent.isEmpty()) return;
        EnumSet<BlightType> blightsOfPos = blightChunkComponent.get().getBlightsOfPos(pos);
        BlightType.applyToStack(stack, blightsOfPos);
        itemEntity.setStack(stack);
    }
}
