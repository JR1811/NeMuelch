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
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.particle.data.SwipeParticleEffect;
import net.shirojr.nemuelch.util.ParticlePacketType;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.data.DamageInstance;
import net.shirojr.nemuelch.util.duck.Generation;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.function.Predicate;

public class MiscEntityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_entity");
    private static final int REBOUND_DAMAGE_INTERVALS = 5;

    private final LivingEntity provider;

    private final Deque<DamageInstance> reboundDamageInstances;
    private boolean activeRebound;

    private @Nullable DamageInstance regainHealthInstance;

    private int pullUpCooldown;
    private int pivotEnchantmentTicks;

    // ----------------------- intentionally non-persistent fields -----------------------
    private static final int particleSpiralTickGap = 2;
    private static final int particleSpiralSequenceTickGap = 30;
    private float itemEntityKillAuraRadius = 10;
    private int itemEntityKillAuraDuration;
    private @Nullable Predicate<ItemStack> itemEntityKillAuraFilter;

    private boolean lockSlowing;

    public MiscEntityComponent(LivingEntity provider) {
        this.provider = provider;
        this.reboundDamageInstances = new ArrayDeque<>();
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
        if (this.reboundDamageInstances.isEmpty()) {
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

    public Deque<DamageInstance> getReboundDamageInstances() {
        return reboundDamageInstances;
    }

    public @Nullable DamageInstance getRegainHealthInstance() {
        return regainHealthInstance;
    }

    public void setRegainHealthInstance(@Nullable DamageInstance regainDamageInstance) {
        this.regainHealthInstance = regainDamageInstance;
        this.sync();
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

    public int getPivotEnchantmentTicks() {
        return pivotEnchantmentTicks;
    }

    private void setPivotEnchantmentTicks(int pivotEnchantmentTicks) {
        this.pivotEnchantmentTicks = pivotEnchantmentTicks;
    }

    public void startPivotSequence() {
        if (!(this.provider.getWorld() instanceof ServerWorld serverWorld)) return;
        int ticks = serverWorld.getGameRules().getInt(NemuelchGameRules.BUCKLER_SHIELD_DASH_PIVOT_DELAY);
        this.setPivotEnchantmentTicks(ticks);
    }

    private void onPivot() {
        float newYaw = this.provider.getHeadYaw() + 180;
        this.provider.setYaw(newYaw);
        this.provider.setHeadYaw(newYaw);
        this.provider.setBodyYaw(newYaw);

        Vec3d newDirection = this.provider.getRotationVec(1).multiply(0.3).add(0, 0.4, 0);
        this.provider.setVelocity(newDirection);
        this.provider.velocityDirty = true;

        if (this.provider.getWorld() instanceof ServerWorld serverWorld) {
            PlayerLookupUtil.trackingAndSelf(this.provider).forEach(player ->
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(this.provider))
            );
            if (this.provider instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(0, 0, 0, newYaw, serverPlayer.getPitch(),
                        Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z), 0));
            }
            serverWorld.playSound(null, this.provider.getX(), this.provider.getY(), this.provider.getZ(),
                    NeMuelchSounds.SWOOSH, SoundCategory.NEUTRAL, 1f, 0.7f);
        }
    }

    @Override
    public void tick() {
        World world = provider.getWorld();
        int age = this.provider.age;

        if (this.pullUpCooldown > 0) {
            setPullUpCooldown(getPullUpCooldown() - 1);
        }

        if (!reboundDamageInstances.isEmpty() && this.activeRebound) {
            if (age % REBOUND_DAMAGE_INTERVALS == 0) {
                DamageInstance entry = this.reboundDamageInstances.poll();
                if (entry != null) {
                    provider.damage(entry.source(), entry.damage());
                }
                if (reboundDamageInstances.isEmpty()) {
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
            if (age % 20 == 0 && random.nextFloat() < 0.8 && !(provider instanceof ServerPlayerEntity player && player.isSpectator())) {
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

        if (this.provider.isAlive() && this.provider instanceof Generation generationHolder && world instanceof ServerWorld serverWorld) {
            int generation = generationHolder.nemuelch$getGeneration();
            if (generation != 0) {
                float normalizedIntensity = generationHolder.getNormalizedGeneration(serverWorld);
                int particleCount = MathHelper.floor(15 * normalizedIntensity);
                int sequenceLength = particleCount * particleSpiralTickGap;
                int fullSequenceLength = sequenceLength + particleSpiralSequenceTickGap;

                if (age % fullSequenceLength < sequenceLength && age % particleSpiralTickGap == 0) {
                    int currentIndex = (age % fullSequenceLength) / particleSpiralTickGap;
                    double radius = this.provider.getWidth() + (normalizedIntensity * 0.5);
                    double angle = (2 * Math.PI / particleCount) * currentIndex;

                    Vec3d offset = new Vec3d(
                            Math.sin(angle) * radius,
                            ((double) currentIndex / particleCount) * this.provider.getHeight(),
                            Math.cos(angle) * radius
                    ).add(provider.getPos());

                    serverWorld.spawnParticles(
                            new SwipeParticleEffect(0x9A1226, 20, 0, 90, 0.25f, SwipeParticleEffect.Direction.UP),
                            offset.x, offset.y, offset.z, 1,
                            0, 0, 0, 0.1);

                }
            }
        }

        int oldPivotTicks = this.getPivotEnchantmentTicks();
        if (oldPivotTicks > 0) {
            this.setPivotEnchantmentTicks(oldPivotTicks - 1);
            if (this.getPivotEnchantmentTicks() <= 0 && this.provider.isSneaking()) {
                this.onPivot();
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        RegistryWrapper.WrapperLookup registries = provider.getWorld().getRegistryManager();

        if (tag.contains(NeMuelchNbtKeys.PULL_UP_COOLDOWN)) {
            this.pullUpCooldown = tag.getInt(NeMuelchNbtKeys.PULL_UP_COOLDOWN);
        }
        if (tag.contains(NeMuelchNbtKeys.REBOUND_DAMAGE)) {
            reboundDamageInstances.clear();
            NbtList list = tag.getList(NeMuelchNbtKeys.REBOUND_DAMAGE, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                reboundDamageInstances.offer(DamageInstance.fromNbt(list.getCompound(i), registries));
            }
        }

        this.lockSlowing = tag.contains(NeMuelchNbtKeys.LOCKED_SLOWING) && tag.getBoolean(NeMuelchNbtKeys.LOCKED_SLOWING);

        if (tag.contains(NeMuelchNbtKeys.REGAIN_HEALTH_INSTANCE)) {
            this.setRegainHealthInstance(DamageInstance.fromNbt(tag.getCompound(NeMuelchNbtKeys.REGAIN_HEALTH_INSTANCE), registries));
        } else {
            this.setRegainHealthInstance(null);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        RegistryWrapper.WrapperLookup registries = provider.getWorld().getRegistryManager();

        tag.putInt(NeMuelchNbtKeys.PULL_UP_COOLDOWN, this.pullUpCooldown);

        NbtList list = new NbtList();
        for (DamageInstance instance : reboundDamageInstances) {
            list.add(instance.createNbt(registries));
        }
        tag.put(NeMuelchNbtKeys.REBOUND_DAMAGE, list);

        tag.putBoolean(NeMuelchNbtKeys.LOCKED_SLOWING, this.lockSlowing);

        if (this.regainHealthInstance != null) {
            tag.put(NeMuelchNbtKeys.REGAIN_HEALTH_INSTANCE, this.regainHealthInstance.createNbt(registries));
        } else {
            tag.remove(NeMuelchNbtKeys.REGAIN_HEALTH_INSTANCE);
        }
    }

    public void sync() {
        if (this.provider.getWorld().isClient()) return;
        NeMuelchComponents.MISC_ENTITY.sync(this.provider);
    }
}
