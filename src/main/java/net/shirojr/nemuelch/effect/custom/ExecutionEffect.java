package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.effect.util.StatusEffectLifecycle;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchSounds;

public class ExecutionEffect extends StatusEffect implements StatusEffectLifecycle {
    public ExecutionEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        double normalizedHealth = MathHelper.clamp(entity.getHealth() / entity.getMaxHealth(), 0, 1);
        double normalizedThreshold = MathHelper.clamp((amplifier + 1) * 0.01, 0, 1);
        if (normalizedHealth > normalizedThreshold) return;
        entity.damage(NeMuelchDamageTypes.of(entity.getWorld(), NeMuelchDamageTypes.EXECUTION), entity.getHealth());
    }

    @Override
    public void onStarted(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        Vec3d pos = entity.getPos();
        serverWorld.playSound(null, pos.x, pos.y, pos.z, NeMuelchSounds.LAUGH_EXECUTIONER, SoundCategory.MASTER, 5f, 1f);
    }

    @Override
    public void onFinished(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!entity.isAlive()) return;
        Vec3d pos = entity.getPos();
        serverWorld.playSound(null, pos.x, pos.y, pos.z, NeMuelchSounds.SIGH_EXECUTIONER, SoundCategory.MASTER, 5f, 1f);
    }
}
