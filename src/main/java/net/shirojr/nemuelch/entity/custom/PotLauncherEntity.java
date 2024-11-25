package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;

public class PotLauncherEntity extends Entity {
    private static final EulerAngle DEFAULT_ANGLES = new EulerAngle(1.0F, 0.0F, 1.0F);
    private static final TrackedData<EulerAngle> ANGLES = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);

    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
    }

    public PotLauncherEntity(World world, Vec3d pos) {
        this(world);
        this.setPosition(pos);
        this.setAngles(DEFAULT_ANGLES);
    }

    public EulerAngle getAngles() {
        return this.dataTracker.get(ANGLES);
    }

    protected void setAngles(EulerAngle angles) {
        this.dataTracker.set(ANGLES, angles);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ANGLES, DEFAULT_ANGLES);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 16.0;
        d *= 64.0 * getRenderDistanceMultiplier();
        return distance < d * d;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        NbtList anglesNbt = nbt.getList("angles", NbtElement.FLOAT_TYPE);
        this.setAngles(anglesNbt.isEmpty() ? DEFAULT_ANGLES : new EulerAngle(anglesNbt));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!DEFAULT_ANGLES.equals(this.getAngles())) {
            nbt.put("angles", this.getAngles().toNbt());
        }
    }
}
