package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class SlowActingPoisonEffect extends StatusEffect {
    private final Text onApplied;
    private final Text onStartedActing;
    private final float secondPhaseDurationMultiplier;
    private final float secondPhaseAmplifierMultiplier;

    public SlowActingPoisonEffect(int color, Text onApplied, Text onStartedActing,
                                  float secondPhaseDurationMultiplier, float secondPhaseAmplifierMultiplier) {
        super(StatusEffectCategory.HARMFUL, color);
        this.onApplied = onApplied;
        this.onStartedActing = onStartedActing;
        this.secondPhaseDurationMultiplier = secondPhaseDurationMultiplier;
        this.secondPhaseAmplifierMultiplier = secondPhaseAmplifierMultiplier;
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(this.onApplied, true);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(this.onStartedActing, true);
        }
        this.startSecondPhase(entity);
    }

    public void startSecondPhase(LivingEntity entity) {
        // entity.addStatusEffect(new StatusEffectInstance());
    }

    public enum Type {
        POISON(),
        WITHER(),
        EXECUTION()
    }

    public record FirstPhaseMemory(int duration, int amplifier) {

    }
}
