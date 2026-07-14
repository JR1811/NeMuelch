package net.shirojr.nemuelch.monster;

import net.minecraft.entity.LivingEntity;

public interface MonsterTransitionCallback {
    default void onMonsterTypeGained(LivingEntity provider) {

    }

    default void onMonsterTypeLost(LivingEntity provider) {

    }
}
