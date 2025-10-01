package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;isWaterNearby(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean regressOnBlight(WorldView world, BlockPos pos, Operation<Boolean> original) {
        boolean isWaterNearby = original.call(world, pos);
        if (!isWaterNearby) return false;
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(world.getChunk(pos));
        if (blightChunkComponent.isEmpty()) return true;
        boolean hasCorruptBlight = blightChunkComponent.get().isBlighted(pos, BlightType.CORRUPTED);
        return !hasCorruptBlight;
    }
}
