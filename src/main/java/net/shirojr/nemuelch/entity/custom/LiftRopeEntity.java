package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.init.NeMuelchTrackedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LiftRopeEntity extends Entity {
    public static final float SIZE = 0.25f;

    private static final TrackedData<Optional<BlockPos>> START_ANCHOR = DataTracker.registerData(LiftRopeEntity.class, NeMuelchTrackedData.OPTIONAL_BLOCKPOS);
    private static final TrackedData<Optional<BlockPos>> END_ANCHOR = DataTracker.registerData(LiftRopeEntity.class, NeMuelchTrackedData.OPTIONAL_BLOCKPOS);

    public LiftRopeEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public LiftRopeEntity(World world, @Nullable BlockPos startAnchor, @Nullable BlockPos endAnchor) {
        this(NeMuelchEntities.LIFT_ROPE, world);
        this.setStartAnchor(startAnchor);
        this.setEndAnchor(endAnchor);
    }


    //region Getter & Setter
    public Optional<BlockPos> getStartAnchor() {
        return this.dataTracker.get(START_ANCHOR);
    }

    public void setStartAnchor(@Nullable BlockPos startAnchor) {
        this.dataTracker.set(START_ANCHOR, Optional.ofNullable(startAnchor));
    }

    public Optional<BlockPos> getEndAnchor() {
        return this.dataTracker.get(END_ANCHOR);
    }

    public void setEndAnchor(@Nullable BlockPos endAnchor) {
        this.dataTracker.set(END_ANCHOR, Optional.ofNullable(endAnchor));
    }
    //endregion


    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(START_ANCHOR, Optional.empty());
        this.dataTracker.startTracking(END_ANCHOR, Optional.empty());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

}
