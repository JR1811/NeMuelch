package net.shirojr.nemuelch.util.interaction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffectInstance;

/**
 * Provides more StatusEffect removal data compared to already existing
 * {@link net.minecraft.entity.effect.StatusEffect#onRemoved(LivingEntity, AttributeContainer, int) StatusEffect.onRemoved()}
 */
public interface EffectRemoval {
    /**
     * Runs both on client and logical server side
     */
    void onStatusEffectRemoved(LivingEntity entity, StatusEffectInstance instance);
}
