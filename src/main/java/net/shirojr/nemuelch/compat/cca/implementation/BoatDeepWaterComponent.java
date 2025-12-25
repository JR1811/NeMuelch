package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.client.NeMuelchClientCache;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.mixin.access.BoatEntityAccess;

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

    public void leaveDeepWater() {
        setDeepWaterTicks(-1, true);
        sync();
    }

    public int getMaxDeepWaterEnduranceTicks() {
        World world = provider.getWorld();
        if (world.isClient()) {
            return NeMuelchClientCache.boatDeepWaterEnduranceTicks;
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

    public boolean shouldCheckDeepWater() {
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
        if (tickedInDeepWater()) {
            this.deepWaterTicks++;
        }

        if (!(provider.getWorld() instanceof ServerWorld serverWorld)) return;

        if (shouldSink()) {
            if (isOnWater()) {
                sink();
            }
            return;
        }
        if (!shouldCheckDeepWater()) return;
        boolean tickedDeepWater = tickedInDeepWater();

        int deepWaterLevel = provider.getWorld().getGameRules().getInt(NemuelchGameRules.BOAT_DEEP_WATER_DEPTH);
        boolean isCurrentlyInDeepWater = isInDeepWater(serverWorld, provider.getBlockPos(), deepWaterLevel, (world, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOf(Blocks.WATER)) return true;
            FluidState fluidState = world.getFluidState(blockPos);
            return fluidState.isIn(FluidTags.WATER) && !blockState.isSolidBlock(world, blockPos);
        });

        if (isCurrentlyInDeepWater) {
            if (!tickedDeepWater) {
                enterDeepWater();
            }
        } else if (tickedDeepWater) {
            leaveDeepWater();
        }
        resetTickPauseUntilNextCheck(isCurrentlyInDeepWater);
    }

    public void sync() {
        if (!(provider.getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.BOAT_DEEP_WATER_SWIMMING.sync(this.provider);
    }
}
