package net.shirojr.nemuelch.occasion.util;

import net.minecraft.entity.LivingEntity;

public interface EntityXPModifier {
    default int getModifiedXp(int original, LivingEntity entity, int generation) {
        return original;
    }
}
