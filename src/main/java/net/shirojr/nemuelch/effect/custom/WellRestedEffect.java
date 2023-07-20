package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;

public class WellRestedEffect extends StatusEffect {
    public WellRestedEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isBeneficial() { return true; }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!(entity instanceof PlayerEntity player) || entity.getWorld().isClient) return;
        player.sendMessage(new TranslatableText("chat.nemuelch.effect.wellRested"), true);
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {

        super.onRemoved(entity, attributes, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 200 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity instanceof PlayerEntity player) || entity.getWorld().isClient) return;
        player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        super.applyUpdateEffect(entity, amplifier);
    }
}
