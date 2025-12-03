package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.ParticlePacketType;
import org.jetbrains.annotations.NotNull;

public class MiscEntityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_entity");

    private final LivingEntity provider;

    public MiscEntityComponent(LivingEntity provider) {
        this.provider = provider;
    }

    public LivingEntity getProvider() {
        return provider;
    }


    @Override
    public void tick() {
        World world = provider.getWorld();

        StatusEffectInstance playthingEffect = provider.getStatusEffect(NeMuelchEffects.PLAYTHING_OF_THE_UNSEEN_DEITY);

        if (playthingEffect != null) {
            Random random = provider.getRandom();
            if (provider.age % 20 == 0 && random.nextFloat() < 0.8 && !(provider instanceof ServerPlayerEntity player && player.isSpectator())) {
                if (world instanceof ServerWorld serverWorld) {
                    double push = (playthingEffect.getAmplifier() + 1) * 1.5;
                    float kickDamage = 4f;

                    float pitch = MathHelper.lerp(random.nextFloat(), 0.8f, 1f);
                    world.playSound(null, provider.getX(), provider.getY(), provider.getZ(), NeMuelchSounds.HIT_DEITY, SoundCategory.NEUTRAL, 1f, pitch);
                    double x = world.getRandom().nextDouble() * push - (push * 0.5);
                    double y = Math.abs(world.getRandom().nextDouble() * (push * 0.5));
                    double z = world.getRandom().nextDouble() * push - (push * 0.5);
                    provider.setVelocity(x, y, z);
                    provider.handleFallDamage(provider.getSafeFallDistance(), 0.2F, world.getDamageSources().fall());
                    provider.velocityModified = true;

                    if (provider.getHealth() > kickDamage) {
                        provider.damage(world.getDamageSources().magic(), kickDamage);
                    }

                    int particleAmount = 150;
                    float particleSpread = 0.5f;
                    float verticalParticleSpread = 3f;
                    for (int i = 0; i < particleAmount; i++) {
                        double particleX = provider.getX() + ((world.getRandom().nextDouble() - 0.5) * 2) * particleSpread;
                        double particleY = provider.getY() + ((world.getRandom().nextDouble() - 0.5) * 2) * verticalParticleSpread;
                        double particleZ = provider.getZ() + ((world.getRandom().nextDouble() - 0.5) * 2) * particleSpread;
                        BlockPos pos = BlockPos.ofFloored(particleX, particleY, particleZ);

                        PlayerLookup.tracking(serverWorld, provider.getBlockPos()).forEach(target -> {
                            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                            buf.writeBlockPos(pos);
                            buf.writeEnumConstant(ParticlePacketType.EFFECT_PLAYTHING_OF_THE_UNSEEN_DEITY);
                            ServerPlayNetworking.send(target, NetworkIdentifiers.PLAY_PARTICLE_S2C, buf);
                        });
                    }
                }
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {

    }

    public void sync() {
        NeMuelchComponents.MISC_ENTITY.sync(this.provider);
    }
}
