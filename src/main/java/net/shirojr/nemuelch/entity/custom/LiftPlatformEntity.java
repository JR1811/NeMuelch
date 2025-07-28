package net.shirojr.nemuelch.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchTrackedData;
import net.shirojr.nemuelch.util.helper.LiftSystemManager;
import net.shirojr.nemuelch.util.wrapper.Mass;

import java.util.HashMap;
import java.util.Optional;

public class LiftPlatformEntity extends Entity implements Mass {
    private static final TrackedData<Optional<Vec3d>> ANCHOR = DataTracker.registerData(LiftPlatformEntity.class, NeMuelchTrackedData.OPTIONAL_POS);
    private static final TrackedData<Float> MASS = DataTracker.registerData(LiftPlatformEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public final HashMap<DefaultedList<ItemStack>, Block> storedInventories = new HashMap<>();

    public LiftPlatformEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ANCHOR, Optional.empty());
        this.dataTracker.startTracking(MASS, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        //LiftSystemManager.getOrCreate(this, );
    }

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
        super.onRemoved();
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

    @Override
    public double getMass() {
        return 0;
    }

    @Override
    public void setMass(float mass) {

    }
}
