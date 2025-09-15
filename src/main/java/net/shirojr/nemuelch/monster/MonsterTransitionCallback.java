package net.shirojr.nemuelch.monster;

import net.minecraft.entity.LivingEntity;

public interface MonsterTransitionCallback {
    default void onMonsterTypeGainedDominance(LivingEntity provider) {

    }

    default void onMonsterTypeLostDominance(LivingEntity provider) {

    }
}
