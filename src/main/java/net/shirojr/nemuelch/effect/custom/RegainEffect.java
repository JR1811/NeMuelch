package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.data.DamageInstance;
import net.shirojr.nemuelch.util.interaction.EffectRemoval;

public class RegainEffect extends StatusEffect implements EffectRemoval {
    public RegainEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public DamageInstance getRegainedHealth(StatusEffectInstance effectInstance, DamageInstance damage) {
        int amplifier = effectInstance.getAmplifier();
        float multiplier = (amplifier + 1) * 0.01f;
        return new DamageInstance(damage.source(), damage.damage() * multiplier);
    }

    public void applyStoredRegainHealthInstance(LivingEntity damageDealer, float appliedDamage) {
        MiscEntityComponent component = MiscEntityComponent.get(damageDealer);
        DamageInstance regainHealthInstance = component.getRegainHealthInstance();
        if (regainHealthInstance == null) return;
        damageDealer.heal(Math.min(regainHealthInstance.damage(), appliedDamage));
        component.setRegainHealthInstance(null);
        if (damageDealer.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, damageDealer.getBlockPos(), NeMuelchSounds.HIT_REGAIN, SoundCategory.NEUTRAL, 2f, 1f);
        }
    }

    @Override
    public void onStatusEffectRemoved(LivingEntity entity, StatusEffectInstance instance) {
        MiscEntityComponent component = MiscEntityComponent.get(entity);
        component.setRegainHealthInstance(null);
    }
}
