package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
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
import net.shirojr.nemuelch.effect.custom.ReboundEffect;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.ParticlePacketType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

public class MiscEntityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_entity");
    private static final int reboundDamageIntervals = 5;

    private final LivingEntity provider;

    private final Deque<ReboundEffect.DamageInstance> reboundDamages;
    private boolean activeRebound;

    private int pullUpCooldown;

    private float itemEntityKillAuraRadius = 10;                        // intentionally non-persistent
    private int itemEntityKillAuraDuration;                             // intentionally non-persistent
    private @Nullable Predicate<ItemStack> itemEntityKillAuraFilter;    // intentionally non-persistent

    private boolean lockSlowing;

    public MiscEntityComponent(LivingEntity provider) {
        this.provider = provider;
        this.reboundDamages = new ArrayDeque<>();
        this.activeRebound = false;
        this.lockSlowing = false;
    }

    public static MiscEntityComponent get(LivingEntity entity) {
        return NeMuelchComponents.MISC_ENTITY.get(entity);
    }

    public LivingEntity getProvider() {
        return provider;
    }


    public int getPullUpCooldown() {
        return pullUpCooldown;
    }

    public boolean isPullUpOnCooldown() {
        return this.pullUpCooldown > 0;
    }

    public void setPullUpCooldown(int pullUpCooldown) {
        int previousCooldown = this.pullUpCooldown;
        this.pullUpCooldown = Math.max(0, pullUpCooldown);
        if (previousCooldown == 0 ^ this.pullUpCooldown == 0) {
            this.sync();
        }
    }

    public void startRebound() {
        if (this.reboundDamages.isEmpty()) {
            stopRebound();
            return;
        }
        this.activeRebound = true;
    }

    public void stopRebound() {
        this.activeRebound = false;
    }

    public boolean isApplyingRebound() {
        return this.activeRebound;
    }

    public Deque<ReboundEffect.DamageInstance> getReboundDamages() {
        return reboundDamages;
    }

    public float getItemEntityKillAuraRadius() {
        return itemEntityKillAuraRadius;
    }

    public void setItemEntityKillAuraRadius(float itemEntityKillAuraRadius) {
        if (itemEntityKillAuraRadius <= 0) return;
        this.itemEntityKillAuraRadius = itemEntityKillAuraRadius;
        sync();
    }

    public int getItemEntityKillAuraDuration() {
        return itemEntityKillAuraDuration;
    }

    public void setItemEntityKillAuraDuration(int itemEntityKillAuraDuration) {
        int prevDuration = getItemEntityKillAuraDuration();
        this.itemEntityKillAuraDuration = Math.max(0, itemEntityKillAuraDuration);
        if (prevDuration == getItemEntityKillAuraDuration()) return;
        if (prevDuration != 0 && getItemEntityKillAuraDuration() != 0) return;
        sync();
    }

    @Nullable
    public Predicate<ItemStack> getItemEntityKillAuraFilter() {
        return itemEntityKillAuraFilter;
    }

    public void setItemEntityKillAuraFilter(@Nullable Predicate<ItemStack> itemEntityKillAuraFilter) {
        this.itemEntityKillAuraFilter = itemEntityKillAuraFilter;
    }

    public boolean shouldKillItemEntity(ItemEntity target) {
        if (getItemEntityKillAuraDuration() <= 0) return false;
        if (getItemEntityKillAuraFilter() != null) {
            if (!getItemEntityKillAuraFilter().test(target.getStack())) {
                return false;
            }
        }
        return !(provider.squaredDistanceTo(target) > getItemEntityKillAuraRadius() * getItemEntityKillAuraRadius());
    }

    public boolean isSlowingLocked() {
        return lockSlowing;
    }

    public void setSlowingLock(boolean locked, boolean shouldSync) {
        this.lockSlowing = locked;
        if (shouldSync) {
            this.sync();
        }
    }

    @Override
    public void tick() {
        World world = provider.getWorld();

        if (this.pullUpCooldown > 0) {
            setPullUpCooldown(getPullUpCooldown() - 1);
        }

        if (!reboundDamages.isEmpty() && this.activeRebound) {
            if (provider.age % reboundDamageIntervals == 0) {
                ReboundEffect.DamageInstance entry = this.reboundDamages.poll();
                if (entry != null) {
                    provider.damage(entry.source(), entry.damage());
                }
                if (reboundDamages.isEmpty()) {
                    this.stopRebound();
                }
            }
        }

        if (world instanceof ServerWorld serverWorld && getItemEntityKillAuraDuration() > 0) {
            serverWorld.getOtherEntities(
                    getProvider(),
                    provider.getBoundingBox().expand(getItemEntityKillAuraRadius()),
                    entity -> entity instanceof ItemEntity itemEntity && shouldKillItemEntity(itemEntity)
            ).forEach(entity -> entity.damage(entity.getDamageSources().outOfWorld(), Integer.MAX_VALUE));
            setItemEntityKillAuraDuration(getItemEntityKillAuraDuration() - 1);
        }

        StatusEffectInstance playthingEffect = provider.getStatusEffect(NeMuelchStatusEffects.PLAYTHING_OF_THE_UNSEEN_DEITY);
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
        if (tag.contains("pullUpCooldown")) {
            this.pullUpCooldown = tag.getInt("pullUpCooldown");
        }
        if (tag.contains("ReboundDamage")) {
            reboundDamages.clear();
            RegistryWrapper.WrapperLookup registries = provider.getWorld().getRegistryManager();
            NbtList list = tag.getList("ReboundDamage", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                reboundDamages.offer(ReboundEffect.DamageInstance.fromNbt(list.getCompound(i), registries));
            }
        }

        this.lockSlowing = tag.contains("lockedSlowing") && tag.getBoolean("lockedSlowing");

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.putInt("pullUpCooldown", this.pullUpCooldown);

        NbtList list = new NbtList();
        RegistryWrapper.WrapperLookup registries = provider.getWorld().getRegistryManager();
        for (ReboundEffect.DamageInstance instance : reboundDamages) {
            list.add(instance.toNbt(registries));
        }
        tag.put("ReboundDamage", list);

        tag.putBoolean("lockedSlowing", this.lockSlowing);
    }

    public void sync() {
        if (this.provider.getWorld().isClient()) return;
        NeMuelchComponents.MISC_ENTITY.sync(this.provider);
    }
}
