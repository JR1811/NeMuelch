package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.shirojr.nemuelch.effect.NeMuelchEffects;

public class LevitatingAbsolutionEffect extends StatusEffect {
    public LevitatingAbsolutionEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!entity.getWorld().isClient()) {
            entity.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.SHIELDING_SKIN,
                    150, 0, true, false));

            entity.setNoGravity(true);
            entity.setVelocity(0, 0.2 * (amplifier + 1), 0);
            entity.velocityModified = true;
        }

        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!entity.getWorld().isClient()) {
            entity.setNoGravity(false);
            entity.removeStatusEffect(NeMuelchEffects.SHIELDING_SKIN);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 150, 1, true, false));
        }

        super.onRemoved(entity, attributes, amplifier);
    }
}
