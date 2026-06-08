package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.event.custom.AcidCallbacks;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.mixin.access.EntityAccess;
import net.shirojr.nemuelch.network.NeMuelchCache;
import net.shirojr.nemuelch.particle.data.SwipeParticleEffect;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.NotNull;

public class AcidEntityComponent implements Component, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("acid_entity");
    public static final int DEFAULT_ACID_MAX_TICKS = 600;

    private final LivingEntity entity;
    private int acidTicks = 0;
    private boolean isImmune;

    public AcidEntityComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public static AcidEntityComponent get(LivingEntity entity) {
        return NeMuelchComponents.ACID_ENTITY.get(entity);
    }

    public int getAcidTicks() {
        return acidTicks;
    }

    public void setAcidTicks(int acidTicks) {
        if (!this.entity.getWorld().isClient()) {
            int old = this.getAcidTicks();
            int maxAcidTicks = getMaxAcidTicks();
            this.acidTicks = MathHelper.clamp(acidTicks, 0, maxAcidTicks);

            if (old == 0 && this.acidTicks != 0) {
                if (this.entity instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.translatable("info.nemuelch.atmospheric_acid.start"), true);
                }
            } else if (old != 0 && this.acidTicks == 0) {
                if (this.entity instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.translatable("info.nemuelch.atmospheric_acid.end"), true);
                }
            }

            if ((old == 0 ^ this.getAcidTicks() == 0) || (old >= maxAcidTicks ^ this.acidTicks >= maxAcidTicks)) {
                this.sync();
            }
        }
    }

    public boolean isImmune() {
        return isImmune;
    }

    public void setImmune(boolean immune) {
        this.isImmune = immune;
        if (this.entity.getWorld() instanceof ServerWorld) {
            this.sync();
        }
    }

    public static void onMaxAcidTick(LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(NeMuelchStatusEffects.ACID_BURN, 6000, 0, false, false, true));
    }

    public static void onDirectContact(LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(NeMuelchStatusEffects.ACID_BURN, 300, 3, false, false, true));
    }

    public int getMaxAcidTicks() {
        return NeMuelchCache.getMaxAcidTicks(this.entity.getWorld());
    }

    public boolean isMaxedOnAcid() {
        int ticks = this.getAcidTicks();
        if (ticks <= 0) return false;
        return ticks >= getMaxAcidTicks();
    }

    public boolean isAcidicAtmosphereProtected(ServerWorld serverWorld) {
        if (!serverWorld.getGameRules().getBoolean(NemuelchGameRules.ENABLE_ACIDIC_ATMOSPHERE_CHECK)) {
            return true;
        }
        if (serverWorld.isRaining() && serverWorld.isSkyVisible(this.entity.getBlockPos())) {
            return false;
        }
        return AcidCallbacks.IS_ATMOSPHERE_PROTECTED.invoker().isAtmosphereProtected(this.entity);
    }

    public static boolean isInAtmosphericAcid(World world, BlockPos pos) {
        return world.getBiome(pos).isIn(NeMuelchTags.Biomes.ACIDIC);
    }

    public static boolean isInAtmosphericAcid(Entity entity) {
        return isInAtmosphericAcid(entity.getWorld(), entity.getBlockPos());
    }

    public static boolean isInAcidicWater(Entity entity) {
        return entity.getFluidHeight(FluidTags.WATER) > 0.0f && isInAtmosphericAcid(entity);
    }

    public static boolean isInNonAcidicWater(Entity entity) {
        return entity.getFluidHeight(FluidTags.WATER) > 0.0f && !isInAtmosphericAcid(entity);
    }

    public static boolean isInAcidFluid(Entity entity) {
        if (((EntityAccess) entity).firstUpdate()) return false;
        if (entity.getFluidHeight(NeMuelchTags.Fluids.ACID) > 0.0f) return true;
        return isInAcidicWater(entity);
    }

    public static boolean isDirectAcidContactProtected(Entity entity) {
        return AcidCallbacks.IS_DIRECT_CONTACT_PROTECTED.invoker().isContactProtected(entity);
    }

    @Override
    public void serverTick() {
        if (!(this.entity.getWorld() instanceof ServerWorld serverWorld)) return;
        boolean inAtmosphericAcid = isInAtmosphericAcid(this.entity);
        boolean isAtmosphereProtected = this.isAcidicAtmosphereProtected(serverWorld);
        if (!inAtmosphericAcid || isAtmosphereProtected || isImmune()) {
            if (this.getAcidTicks() > 0) {
                this.setAcidTicks(this.getAcidTicks() - 1);
            }
        }
        if (!isImmune()) {
            int atmosphericAcidIntervalCheck = serverWorld.getGameRules().getInt(NemuelchGameRules.ACIDIC_ATMOSPHERE_CHECK_INTERVAL);
            if (this.entity.age % atmosphericAcidIntervalCheck == 0) {
                if (!isMaxedOnAcid()) {
                    if (inAtmosphericAcid && !isAtmosphereProtected) {
                        this.setAcidTicks(this.getAcidTicks() + atmosphericAcidIntervalCheck);
                    }
                }
            }
            if (AcidEntityComponent.isInAcidFluid(this.entity) && !AcidEntityComponent.isDirectAcidContactProtected(this.entity)) {
                onDirectContact(this.entity);
                this.setAcidTicks(this.getMaxAcidTicks());
            } else if (this.getAcidTicks() >= getMaxAcidTicks() && !this.entity.hasStatusEffect(NeMuelchStatusEffects.ACID_BURN)) {
                onMaxAcidTick(this.entity);
            }
            if (entity.hasStatusEffect(NeMuelchStatusEffects.ACID_BURN) && isInNonAcidicWater(this.entity)) {
                entity.removeStatusEffect(NeMuelchStatusEffects.ACID_BURN);
                serverWorld.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.NEUTRAL, 2f, 0.65f);
            }
            StatusEffectInstance acidBurnStatusEffectInstance = entity.getStatusEffect(NeMuelchStatusEffects.ACID_BURN);
            if (acidBurnStatusEffectInstance != null && entity.getWorld().getRandom().nextFloat() < 0.2f) {
                int maxParticleProgress = 50;
                int particleProgress = entity.age % maxParticleProgress;
                double radius = entity.getWidth() * 1.5;
                double angle = (2 * Math.PI / 8) * particleProgress;
                Vec3d offset = new Vec3d(
                        Math.cos(angle) * radius,
                        entity.getHeight() * 0.5,
                        Math.sin(angle) * radius
                ).add(entity.getPos());

                serverWorld.spawnParticles(
                        new SwipeParticleEffect(0x9ae334, 20, 0, 90, 0.25f, SwipeParticleEffect.Direction.DOWN),
                        offset.x, offset.y, offset.z, 1,
                        0, 0, 0, 0.1);
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains(NbtKeys.ACID_TICKS_NBT_KEY)) {
            this.acidTicks = nbt.getInt(NbtKeys.ACID_TICKS_NBT_KEY);
        }
        if (nbt.contains(NbtKeys.ACID_IMMUNITY_NBT_KEY)) {
            this.isImmune = nbt.getBoolean(NbtKeys.ACID_IMMUNITY_NBT_KEY);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        nbt.putInt(NbtKeys.ACID_TICKS_NBT_KEY, this.acidTicks);
        nbt.putBoolean(NbtKeys.ACID_IMMUNITY_NBT_KEY, this.isImmune);
    }

    public void sync() {
        NeMuelchComponents.ACID_ENTITY.sync(this.entity);
    }
}
