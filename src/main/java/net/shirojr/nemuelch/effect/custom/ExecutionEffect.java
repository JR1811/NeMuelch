package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;

public class ExecutionEffect extends StatusEffect {
    public ExecutionEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        double normalizedHealth = MathHelper.clamp(entity.getHealth() / entity.getMaxHealth(), 0, 1);
        double normalizedThreshold = MathHelper.clamp((amplifier + 1) * 0.01, 0, 1);
        if (normalizedHealth > normalizedThreshold) return;
        entity.damage(NeMuelchDamageTypes.of(entity.getWorld(), NeMuelchDamageTypes.EXECUTION), entity.getHealth());
    }
}
