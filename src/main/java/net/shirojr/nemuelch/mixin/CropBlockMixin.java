package net.shirojr.nemuelch.mixin;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin
        extends PlantBlock
        implements Fertilizable {
    @Shadow
    public abstract int getAge(BlockState state);

    @Shadow
    public abstract BlockState withAge(int age);

    public CropBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), cancellable = true)
    private void tickCrops(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (isBlightContaminated(world, pos)) {
            int currentAge = getAge(state);
            world.setBlockState(pos, withAge(Math.max(0, currentAge - 1)));
            ci.cancel();
            return;
        }
        if (isFrozen(world, pos)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean isBlightContaminated(ServerWorld world, BlockPos pos) {
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(world.getChunk(pos));
        if (blightChunkComponent.isEmpty()) return false;
        BlightChunkComponent component = blightChunkComponent.get();
        return component.isBlighted(pos, BlightType.CORRUPTED) || component.isBlighted(pos, BlightType.WITHERING);
    }

    @Unique
    private static boolean isFrozen(ServerWorld world, BlockPos pos) {
        if (!NeMuelchConfigInit.CONFIG.frozenGroundPreventsCropBlockGrowth) return false;
        RegistryEntry<Biome> cropBlockBiome = world.getBiome(pos);
        return cropBlockBiome.isIn(ConventionalBiomeTags.CLIMATE_COLD);
    }
}
