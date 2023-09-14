package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.WateringCanBlock;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

public class WateringCanBlockEntity extends BlockEntity {
    private int fillState, tickCounter;
    private WateringCanHelper.ItemMaterial material;
    private boolean shouldFill;
    public static final int FILL_RATE = 10, FILL_CHANCE = 3;

    public WateringCanBlockEntity(BlockPos pos, BlockState state, WateringCanHelper.ItemMaterial material) {
        super(NeMuelchBlockEntities.WATERING_CAN, pos, state);
        this.fillState = 0;
        this.tickCounter = 0;
        this.shouldFill = false;
        this.material = material;
    }

    public WateringCanBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, WateringCanHelper.ItemMaterial.COPPER);
    }

    public void setShouldFill(boolean shouldFill) {
        this.shouldFill = shouldFill;
        NeMuelch.devLogger("Should fill set to true");
    }

    public void setMaterial(WateringCanHelper.ItemMaterial material) {
        this.material = material;
    }

    public int getFillState() {
        return fillState;
    }

    public void setFillState (int fillState) {
        this.fillState = fillState;
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, WateringCanBlockEntity entity) {
        if (world.isClient()) return;
        if (!(blockState.getBlock() instanceof WateringCanBlock wateringCanBlock)) return;
        if (blockState.get(NeMuelchProperties.FILLED) || !entity.shouldFill) return;

        if (!(world instanceof ServerWorld serverWorld)) return;

        entity.tickCounter++;
        if (entity.tickCounter % FILL_RATE == 0) {
            if (world.getRandom().nextInt(FILL_CHANCE) == 0) {
                entity.fillState++;

                serverWorld.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP, blockPos.getX() + 0.5, blockPos.getY() + 0.7, blockPos.getZ() + 0.5,
                        10, 0.0, 0.03, 0.0, 0.2f);

                serverWorld.playSound(null, blockPos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 2f, 1.0f);
            }

            entity.tickCounter = 0;
        }

        if (entity.fillState >= entity.material.getCapacity()) {
            entity.shouldFill = false;
            blockState = blockState.with(NeMuelchProperties.FILLED, true);
        }

        world.setBlockState(blockPos, blockState, Block.NOTIFY_ALL);
        markDirty(world, blockPos, blockState);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(WateringCanHelper.FILL_STATE_NBT_KEY, this.fillState);
        nbt.putBoolean(WateringCanHelper.SHOULD_FILL_NBT_KEY, this.shouldFill);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.fillState = nbt.getInt(WateringCanHelper.FILL_STATE_NBT_KEY);
        this.shouldFill = nbt.getBoolean(WateringCanHelper.SHOULD_FILL_NBT_KEY);
    }
}
