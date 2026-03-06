package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public class DeferredInstantEffect extends StatusEffect {
    private final InstantStatusEffect afterFinish;

    public DeferredInstantEffect(StatusEffectCategory category, InstantStatusEffect afterFinish, int color) {
        super(category, color);
        this.afterFinish = afterFinish;
    }

    public InstantStatusEffect getAfterFinishEffect() {
        return afterFinish;
    }

    public void onFinishedDeference(StatusEffectInstance oldInstance, LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(getAfterFinishEffect(), 1, oldInstance.getAmplifier()));
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);

    }
}
