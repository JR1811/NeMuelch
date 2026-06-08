package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.mixin.access.BoatEntityAccess;
import net.shirojr.nemuelch.network.NeMuelchCache;

import java.util.function.BiPredicate;

public class BoatDeepWaterComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("boat_deep_water_component");

    private final BoatEntity provider;

    private int tickPauseUntilNextCheck;
    private int deepWaterTicks;

    public BoatDeepWaterComponent(BoatEntity provider) {
        this.provider = provider;
        this.deepWaterTicks = -1;
    }

    public static BoatDeepWaterComponent get(BoatEntity provider) {
        return NeMuelchComponents.BOAT_DEEP_WATER_SWIMMING.get(provider);
    }

    public static boolean isInDeepWater(World world, BlockPos searchStartPos, int searchDepth, BiPredicate<World, BlockPos> validWaterCondition) {
        int depth = 0;
        BlockPos.Mutable posWalker = searchStartPos.mutableCopy();
        while (depth < searchDepth) {
            if (!validWaterCondition.test(world, posWalker.toImmutable())) {
                return false;
            }
            posWalker.move(Direction.DOWN);
            depth++;
        }
        return true;
    }

    public int getDeepWaterTicks() {
        return deepWaterTicks;
    }

    public void setDeepWaterTicks(int deepWaterTicks, boolean shouldSync) {
        this.deepWaterTicks = deepWaterTicks;
        if (shouldSync) this.sync();
    }

    public boolean tickedInDeepWater() {
        return getDeepWaterTicks() >= 0;
    }

    public void enterDeepWater() {
        setDeepWaterTicks(0, true);
        sync();
    }

    public void decrementDeepWater(int previousTick, int decrement) {
        if (previousTick < 0) return;
        setDeepWaterTicks(Math.max(previousTick - decrement, -1), true);
        if (getDeepWaterTicks() <= 0) {
            sync();
        }
    }

    public int getMaxDeepWaterEnduranceTicks() {
        World world = provider.getWorld();
        if (world.isClient()) {
            return NeMuelchCache.boatDeepWaterEnduranceTicks;
        } else {
            return world.getGameRules().getInt(NemuelchGameRules.BOAT_DEEP_WATER_ENDURANCE);
        }
    }

    public int getTickPauseUntilNextCheck() {
        return tickPauseUntilNextCheck;
    }

    public void setTickPauseUntilNextCheck(int tickPauseUntilNextCheck) {
        this.tickPauseUntilNextCheck = tickPauseUntilNextCheck;
    }

    public void resetTickPauseUntilNextCheck(boolean fastCheck) {
        int pauseTicks = provider.getWorld().getGameRules().getInt(NemuelchGameRules.BOAT_DEEP_WATER_CHECK_INTERVAL);
        if (fastCheck) {
            pauseTicks = Math.min(5, pauseTicks);
        }
        setTickPauseUntilNextCheck(pauseTicks);
    }

    private void decrementTickPause() {
        int pause = getTickPauseUntilNextCheck();
        if (pause <= 0) return;
        setTickPauseUntilNextCheck(pause - 1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldCheckDeepWater(Entity entity) {
        if (entity.getType().isIn(NeMuelchTags.EntityTypes.UNSINKABLE)) return false;
        GameRules gameRules = provider.getWorld().getGameRules();
        int checkInterval = gameRules.getInt(NemuelchGameRules.BOAT_DEEP_WATER_CHECK_INTERVAL);
        if (checkInterval == -1) return false;
        int deepWaterLevel = gameRules.getInt(NemuelchGameRules.BOAT_DEEP_WATER_DEPTH);
        if (deepWaterLevel == -1) return false;
        return getTickPauseUntilNextCheck() == 0;
    }

    public boolean isOnWater() {
        return ((BoatEntityAccess) provider).neMuelch$checkLocation().equals(BoatEntity.Location.IN_WATER);
    }

    public boolean shouldSink() {
        return getDeepWaterTicks() > getMaxDeepWaterEnduranceTicks();
    }

    private void sink() {
        if (isOnWater() || provider.getWorld() instanceof ServerWorld) {
            Vec3d originalVelocity = provider.getVelocity();
            provider.setVelocity(originalVelocity.x, -0.7, originalVelocity.z);
            provider.velocityModified = true;
        }
    }

    private int getSplashSoundInterval(float progress) {
        if (progress < 0.3f) return 40;
        if (progress < 0.6f) return 20;
        if (progress < 0.8f) return 10;
        return 5;
    }

    private void handleSplashSounds(float progress) {
        if (!(provider.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!isOnWater()) return;

        if (deepWaterTicks % getSplashSoundInterval(progress) == 0) {
            float volume = 0.3f + (progress * 0.7f);
            float pitch = 0.8f + (progress * 0.4f);
            serverWorld.playSound(null, provider.getX(), provider.getY(), provider.getZ(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.NEUTRAL, volume, pitch);
        }

        if (progress > 0.6f && deepWaterTicks % 15 == 0) {
            serverWorld.playSound(null, provider.getX(), provider.getY(), provider.getZ(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.NEUTRAL, 0.5f, 1.0f + progress * 0.5f);
        }
    }

    private void handleParticles(float progress) {
        World world = provider.getWorld();
        if (!(world.isClient())) return;
        if (!isOnWater()) return;
        Random random = world.getRandom();
        int particleCount = (int) (progress * 3) + 1;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (random.nextDouble() - 0.5) * provider.getWidth();
            double offsetZ = (random.nextDouble() - 0.5) * provider.getWidth();
            double x = provider.getX() + offsetX;
            double y = provider.getY() + 0.1;
            double z = provider.getZ() + offsetZ;

            world.addParticle(
                    ParticleTypes.BUBBLE,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.1,
                    random.nextDouble() * 0.1,
                    (random.nextDouble() - 0.5) * 0.1
            );
        }

        if (progress > 0.5f && random.nextFloat() < progress * 0.3f) {
            double x = provider.getX() + (random.nextDouble() - 0.5) * provider.getWidth();
            double y = provider.getY() + 0.3;
            double z = provider.getZ() + (random.nextDouble() - 0.5) * provider.getWidth();

            world.addParticle(
                    ParticleTypes.SPLASH,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.3,
                    0.2,
                    (random.nextDouble() - 0.5) * 0.3
            );
        }

        if (progress > 0.8f) {
            for (int i = 0; i < 10; i++) {
                double x = provider.getX() + (random.nextDouble() - 0.5) * provider.getWidth() * 1.5;
                double y = provider.getY() + 0.5;
                double z = provider.getZ() + (random.nextDouble() - 0.5) * provider.getWidth() * 1.5;

                world.addParticle(
                        ParticleTypes.FALLING_WATER,
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.2,
                        random.nextDouble() * 0.3,
                        (random.nextDouble() - 0.5) * 0.2
                );
            }
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.deepWaterTicks = nbt.getInt("DeepWaterTicks");
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt("DeepWaterTicks", getDeepWaterTicks());
    }


    @Override
    public void tick() {
        this.decrementTickPause();

        if (!shouldCheckDeepWater(this.provider)) return;

        int oldDeepWaterTicks = deepWaterTicks;
        if (tickedInDeepWater()) {
            this.deepWaterTicks++;

            float progress = MathHelper.clamp((float) deepWaterTicks / getMaxDeepWaterEnduranceTicks(), 0, 1);
            handleSplashSounds(progress);
            handleParticles(progress);
        }

        if (!(provider.getWorld() instanceof ServerWorld serverWorld)) return;

        if (shouldSink()) {
            if (isOnWater()) {
                sink();
            }
            return;
        }
        if (!shouldCheckDeepWater(this.provider)) return;
        boolean tickedDeepWater = tickedInDeepWater();

        int deepWaterLevel = provider.getWorld().getGameRules().getInt(NemuelchGameRules.BOAT_DEEP_WATER_DEPTH);
        boolean isCurrentlyInDeepWater = isInDeepWater(serverWorld, provider.getBlockPos(), deepWaterLevel, (world, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(Blocks.WATER)) return true;
            if (!blockState.isIn(NeMuelchTags.Blocks.DEEP_WATER_INCLUSIVE)) return false;
            FluidState fluidState = world.getFluidState(blockPos);
            return fluidState.isIn(FluidTags.WATER);
        });

        if (isCurrentlyInDeepWater) {
            if (!tickedDeepWater) {
                enterDeepWater();
            }
        } else if (tickedDeepWater) {
            decrementDeepWater(oldDeepWaterTicks, 2);
        }
        if (oldDeepWaterTicks > 0 && getDeepWaterTicks() <= 0) {
            resetTickPauseUntilNextCheck(isCurrentlyInDeepWater);
        }
    }

    public void sync() {
        if (!(provider.getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.BOAT_DEEP_WATER_SWIMMING.sync(this.provider);
    }
}
