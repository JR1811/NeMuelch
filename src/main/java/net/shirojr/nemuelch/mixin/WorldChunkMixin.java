package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import net.shirojr.nemuelch.event.custom.BlockStateCallbacks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {
    @Shadow
    @Final
    World world;

    private WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry,
                            long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;"
            )
    )
    private void callBlockStateChangeCallback(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir, @Local ChunkSection section) {
        int sectionX = pos.getX() & 15;
        int sectionY = pos.getY() & 15;
        int sectionZ = pos.getZ() & 15;
        BlockState oldState = section.getBlockState(sectionX, sectionY, sectionZ);
        BlockStateCallbacks.STATE_CHANGED.invoker().onBlockStateChanged(world, pos, oldState, state);
    }
}
