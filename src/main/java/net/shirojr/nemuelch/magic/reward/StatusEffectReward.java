package net.shirojr.nemuelch.magic.reward;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.shirojr.nemuelch.magic.Reward;

public class StatusEffectReward implements Reward {
    private final StatusEffectInstance instance;

    public StatusEffectReward(StatusEffectInstance instance) {
        this.instance = instance;
    }

    @Override
    public void apply(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;
        entity.addStatusEffect(this.instance);
    }
}
