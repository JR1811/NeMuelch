package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.sound.NeMuelchSounds;

public class PlaythingOfTheUnseenDeityEffect extends StatusEffect {
    public PlaythingOfTheUnseenDeityEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        double push = (amplifier + 1) * 1.5;
        float kickDamage = 2f;
        int particleAmount = 150;
        float particleSpread = 1.5f;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return;
        }

        World world = entity.getWorld();

        double x = world.getRandom().nextDouble(push) - (push * 0.5);
        double y = Math.abs(world.getRandom().nextDouble(push * 0.5));
        double z = world.getRandom().nextDouble(push) - (push * 0.5);

        if (!entity.isDead() && entity.hasStatusEffect(NeMuelchEffects.PLAYTHING_OF_THE_UNSEEN_DEITY)) {
            entity.playSound(SoundEvents.ENTITY_AXOLOTL_HURT, 1f, 0.5f);

            if (!world.isClient()) {
                entity.setVelocity(0, 0, 0);
                entity.setVelocity(x, y, z);
                entity.handleFallDamage(entity.getSafeFallDistance(), 0.2F, DamageSource.FALL);
                entity.velocityModified = true;

                if (entity.getHealth() > kickDamage) {
                    entity.damage(DamageSource.MAGIC, kickDamage);
                }

            }

            //FIXME: CLIENT side never runs
            //else {
                entity.setVelocityClient(x, y, z);  // not sure if that's even needed...

                for (int i = 0; i < particleAmount; i++) {
                    double particleX = world.getRandom().nextDouble(entity.getX() - particleSpread, entity.getX() + particleSpread);
                    double particleY = world.getRandom().nextDouble(entity.getY() - particleSpread, entity.getY() + particleSpread);
                    double particleZ = world.getRandom().nextDouble(entity.getZ() - particleSpread, entity.getZ() + particleSpread);
                    world.addParticle(ParticleTypes.SMOKE, particleX, particleY + 1.0, particleZ, 0.0, 2.0, 0.0);
                    world.addParticle(ParticleTypes.ENCHANT,particleX, particleY, particleZ, 0, 2, 0);
                }
            //}

        }
        else {
            entity.removeStatusEffect(NeMuelchEffects.PLAYTHING_OF_THE_UNSEEN_DEITY);
        }


        super.applyUpdateEffect(entity, amplifier);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {

        if (entity.getWorld().isClient()) entity.playSound(SoundEvents.BLOCK_COPPER_BREAK, 1f, 1f);
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!entity.getWorld().isClient()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,
                    30 * (amplifier + 1), 0, true, false));
        }
        else {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.onRemoved(entity, attributes, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
