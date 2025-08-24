package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DummyCloseQuarterEntity extends Entity {
    protected BlockPos attachmentPos;
    protected Direction facing = Direction.NORTH;

    public DummyCloseQuarterEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public DummyCloseQuarterEntity(EntityType<?> type, World world, BlockPos attachmentPos) {
        this(type, world);
        this.attachmentPos = attachmentPos;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) return;
        if (!this.isRemoved() && !this.canStayAttached()) {
            this.discard();
            this.onBreak(null);
        }
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private void onBreak(@Nullable Entity entity) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CALCITE_BREAK, SoundCategory.NEUTRAL);
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.attachmentPos = BlockPos.ofFloored(x, y, z);
        this.updateAttachmentPosition();
        this.velocityDirty = true;
    }

    private void updateAttachmentPosition() {

    }

    public boolean canStayAttached() {
        return true;
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("attachmentPos")) {
            this.attachmentPos = BlockPos.fromLong(nbt.getLong("attachmentPos"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putLong("attachmentPos", this.attachmentPos.asLong());
    }
}
