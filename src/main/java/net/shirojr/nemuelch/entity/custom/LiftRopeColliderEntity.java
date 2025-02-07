package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEntities;

public class LiftRopeColliderEntity extends Entity {
    private LiftRopeEntity parentRope;

    public LiftRopeColliderEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public LiftRopeColliderEntity(World world, LiftRopeEntity parent) {
        this(NeMuelchEntities.LIFT_ROPE_COLLIDER, world);
        this.parentRope = parent;
    }


    // region Getter & Setter
    public LiftRopeEntity getParentRope() {
        return parentRope;
    }

    public void setParentRope(LiftRopeEntity parentRope) {
        this.parentRope = parentRope;
    }
    // endregion


    @Override
    public boolean collidesWith(Entity other) {
        return canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other) && !entity.getType().equals(other.getType());
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public void onRemoved() {
        this.parentRope.getColliders().remove(this);
        super.onRemoved();
    }

    @Override
    protected void initDataTracker() {

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
