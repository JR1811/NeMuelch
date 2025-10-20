package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.component.BlightEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * BlockPos values might return <code>null</code> in case of full chunk blight {@link BlightChunkComponent#getCompleteChunkBlights()}
 */
@SuppressWarnings("unused")
public interface BlightAction {
    default boolean canBlight(ServerWorld world, BlockPos pos, BlockState state, Set<BlightType> types) {
        if (!state.getFluidState().isEmpty()) return false;
        if (state.isIn(NeMuelchTags.Blocks.NEVER_BLIGHT)) return false;
        if (state.isAir() && !types.contains(BlightType.AIRBORNE)) return false;
        return !state.isIn(BlockTags.PICKAXE_MINEABLE);
    }

    default void onApplied(ServerWorld world, BlockPos pos, @Nullable LivingEntity entity) {
    }

    default void onRemoved(ServerWorld world, @Nullable BlockPos pos, long blightAge, @Nullable LivingEntity entity) {
    }

    default void onPickedUp(LivingEntity entity, ItemEntity stack, BlightType type) {
    }

    default void onBlockBroken(ServerWorld world, long blightAge, @Nullable BlockPos pos, PlayerEntity player) {
    }

    default void onBlockStateChanged(ServerWorld world, long blightAge, @Nullable BlockPos pos, BlockState before, BlockState after) {
    }

    default void onSteppedOnBlock(ServerWorld world, long blightAge, @Nullable BlockPos pos, Entity entity) {
    }

    default void onBlockCollision(ServerWorld world, long blightAge, @Nullable BlockPos pos, Entity entity) {
    }

    default void onSuccessfulSpread(ServerWorld world, long blightAge, @Nullable BlockPos source, @NotNull BlockPos target, boolean clearedSource) {
    }

    static void apply(LivingEntity entity, BlightType type, BlightEntityComponent.Severity severity) {
        BlightEntityComponent blightEntityComponent = BlightEntityComponent.get(entity);
        blightEntityComponent.setSeverity(type, severity, false, true);
    }
}
