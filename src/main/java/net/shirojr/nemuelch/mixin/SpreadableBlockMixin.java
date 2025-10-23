package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private static void blightDeath(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(world.getChunk(pos));
        if (blightChunkComponent.isEmpty()) return;
        BlightChunkComponent component = blightChunkComponent.get();
        if (component.isEmpty()) return;
        if (!component.isBlighted(pos, BlightType.CORRUPTED) && component.isBlighted(pos, BlightType.WITHERING)) return;
        cir.setReturnValue(false);
    }
}
