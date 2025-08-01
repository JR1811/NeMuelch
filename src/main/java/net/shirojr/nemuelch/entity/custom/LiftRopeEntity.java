package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.init.NeMuelchTrackedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LiftRopeEntity extends Entity {
    public static final float SIZE = 0.25f;
    private static final TrackedData<Optional<Vec3d>> START_ANCHOR = DataTracker.registerData(LiftRopeEntity.class, NeMuelchTrackedData.OPTIONAL_POS);
    private static final TrackedData<Optional<Vec3d>> END_ANCHOR = DataTracker.registerData(LiftRopeEntity.class, NeMuelchTrackedData.OPTIONAL_POS);
    private static final TrackedData<Double> LENGTH = DataTracker.registerData(LiftRopeEntity.class, NeMuelchTrackedData.DOUBLE);

    private double maxTension;
    private double tension;
    private final List<LiftRopeColliderEntity> colliders;

    public LiftRopeEntity(EntityType<?> type, World world) {
        super(type, world);
        this.maxTension = 0;
        this.tension = 0;
        this.colliders = new ArrayList<>();
    }

    public LiftRopeEntity(World world, @Nullable Vec3d startAnchor, @Nullable Vec3d endAnchor, double maxTension, double length) {
        this(NeMuelchEntities.LIFT_ROPE, world);
        this.setStartAnchor(startAnchor);
        this.setEndAnchor(endAnchor);
        this.setLength(length);
        this.maxTension = maxTension;
    }


    //region Getter & Setter
    public Optional<Vec3d> getStartAnchor() {
        return this.dataTracker.get(START_ANCHOR);
    }

    public void setStartAnchor(@Nullable Vec3d startAnchor) {
        this.dataTracker.set(START_ANCHOR, Optional.ofNullable(startAnchor));
    }

    public Optional<Vec3d> getEndAnchor() {
        return this.dataTracker.get(END_ANCHOR);
    }

    public void setEndAnchor(@Nullable Vec3d endAnchor) {
        this.dataTracker.set(END_ANCHOR, Optional.ofNullable(endAnchor));
    }

    public double getLength() {
        return this.dataTracker.get(LENGTH);
    }

    public void setLength(double length) {
        this.dataTracker.set(LENGTH, length);
    }

    public double getMaxTension() {
        return maxTension;
    }

    public void setMaxTension(double maxTension) {
        this.maxTension = maxTension;
    }

    public double getTension() {
        return tension;
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    public List<LiftRopeColliderEntity> getColliders() {
        return colliders;
    }

    public boolean removeCollider(LiftRopeColliderEntity... entities) {
        return this.getColliders().removeAll(List.of(entities));
    }
    //endregion


    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(START_ANCHOR, Optional.empty());
        this.dataTracker.startTracking(END_ANCHOR, Optional.empty());
        this.dataTracker.startTracking(LENGTH, -1.0);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}
