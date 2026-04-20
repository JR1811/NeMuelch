package net.shirojr.nemuelch.occasion.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.function.DoubleUnaryOperator;

public interface EntityStrengthener {
    default void modifyEntitySpawn(ServerWorld world, Entity entity) {
    }

    default void afterEntityKill(ServerWorld world, Entity attacker, LivingEntity killedEntity) {
    }

    static void modifyBaseAttributeIfPresent(@Nullable EntityAttributeInstance instance, DoubleUnaryOperator valueModifier) {
        if (instance == null) return;
        instance.setBaseValue(valueModifier.applyAsDouble(instance.getBaseValue()));
    }
}
