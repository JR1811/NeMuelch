package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

public class StuckEffect extends StatusEffect {
    public StuckEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(new TranslatableText("chat.nemuelch.effect.stuck.hint"), true);
        }

        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {

        super.onRemoved(entity, attributes, amplifier);
    }
}
