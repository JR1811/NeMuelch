package net.shirojr.nemuelch.effect.custom;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import net.shirojr.nemuelch.util.ParticlePacketType;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;

public class PlaythingOfTheUnseenDeityEffect extends StatusEffect {
    public PlaythingOfTheUnseenDeityEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        double push = (amplifier + 1) * 1.5;
        float kickDamage = 2f;
        int particleAmount = 150;
        float particleSpread = 0.5f;
        float verticalParticleSpread = 3f;

        if (entity instanceof PlayerEntity player) {
            if (player.isSpectator()) return;
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

            for (int i = 0; i < particleAmount; i++) {
                double particleX = entity.getX() + ((world.getRandom().nextDouble() - 0.5) * 2) * particleSpread;
                double particleY = entity.getY() + ((world.getRandom().nextDouble() - 0.5) * 2) * verticalParticleSpread;
                double particleZ = entity.getZ() + ((world.getRandom().nextDouble() - 0.5) * 2) * particleSpread;
                BlockPos pos = new BlockPos(particleX, particleY, particleZ);

                if (world instanceof ServerWorld serverWorld) {
                    PlayerLookup.tracking(serverWorld, entity.getBlockPos()).forEach(player -> {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBlockPos(pos);
                        buf.writeEnumConstant(ParticlePacketType.EFFECT_PLAYTHING_OF_THE_UNSEEN_DEITY);
                        ServerPlayNetworking.send(player, NetworkIdentifiers.PLAY_PARTICLE_S2C, buf);
                    });
                }
            }

        } else {
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
        } else {
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
