package net.shirojr.nemuelch.effect.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;

public interface StatusEffectLifecycle {
    default void onStarted(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
    }

    @SuppressWarnings("unused")
    default void onUpgraded(ServerWorld world, LivingEntity entity, StatusEffectInstance instance, boolean reapplied) {
    }

    /**
     * Called before effect removal side effects are called
     */
    default void onFinished(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
    }
}
