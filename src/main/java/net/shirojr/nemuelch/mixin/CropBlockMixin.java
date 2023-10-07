package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin
        extends PlantBlock
        implements Fertilizable {
    public CropBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$getAvailableMoisture(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        var cropBlockBiome = world.getBiome(pos);
        if (ConfigInit.CONFIG.frozenGroundPreventsCropBlockGrowth) return;
        if (cropBlockBiome.matchesKey(BiomeKeys.SNOWY_PLAINS) || cropBlockBiome.matchesKey(BiomeKeys.SNOWY_SLOPES)) {
            ci.cancel();
        }
    }
}
