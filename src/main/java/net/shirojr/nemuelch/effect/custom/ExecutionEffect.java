package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.effect.util.StatusEffectLifecycle;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchParticleTypes;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import org.jetbrains.annotations.Nullable;

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

    public static void onItemEntityKilled(ItemEntity itemEntity, DamageSource source, @Nullable Entity owner) {
        if (owner instanceof ServerPlayerEntity player) {
            if (source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                player.addStatusEffect(
                        new StatusEffectInstance(
                                NeMuelchStatusEffects.EXECUTION, 20 * 60 * 10, 94,
                                false, false, true
                        )
                );
                ServerWorld serverWorld = player.getServerWorld();
                Vec3d pos = itemEntity.getPos();
                spawnParticleRing(serverWorld, pos.add(0, 1.5, 0), 24, 2);
            }
        }
    }


    @SuppressWarnings("SameParameterValue")
    private static void spawnParticleRing(ServerWorld world, Vec3d center, int amount, double radius) {
        for (int i = 0; i < amount; i++) {
            double angle = (2 * Math.PI) * i / amount;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            world.spawnParticles(NeMuelchParticleTypes.MIASMA_RED,
                    x, center.y + 0.4, z, 1, 0, 0, 0, 0);
        }
    }
}
