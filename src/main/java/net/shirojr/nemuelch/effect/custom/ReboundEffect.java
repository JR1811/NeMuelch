package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;

public class ReboundEffect extends StatusEffect {
    public ReboundEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        MiscEntityComponent component = MiscEntityComponent.get(entity);
        component.startRebound();
    }
}
