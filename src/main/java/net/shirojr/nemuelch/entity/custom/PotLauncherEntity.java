package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.util.EntityInteractionHitBox;

public class PotLauncherEntity extends Entity {
    public static final float HEIGHT = 2.4f;
    public static final float WIDTH = 2.2f;

    private static final double yawHitBoxWidth = 0.25, pitchHitBoxWidth = 0.26;
    private static final Box DEFAULT_PITCH_HITBOX = new Box(- (pitchHitBoxWidth / 2), 1.2, - (pitchHitBoxWidth / 2), (pitchHitBoxWidth / 2), 1.45, (pitchHitBoxWidth / 2)).offset(0.47, 0.7, 0.245);
    public static final Box DEFAULT_YAW_HITBOX = new Box(- (yawHitBoxWidth / 2), 0.6, - (yawHitBoxWidth / 2), (yawHitBoxWidth / 2), 1.5, (yawHitBoxWidth / 2)).offset(-0.47, 0, 0.25);

    private static final EulerAngle DEFAULT_ANGLES = new EulerAngle(1.0F, 0.0F, 0.0F);
    private static final TrackedData<EulerAngle> ANGLES = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private Box pitchLeverHitBox, yawPullerHitbox;

    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
        this.pitchLeverHitBox = DEFAULT_PITCH_HITBOX;
        this.yawPullerHitbox = DEFAULT_YAW_HITBOX;
    }

    public PotLauncherEntity(World world, Vec3d pos) {
        this(world);
        this.setPosition(pos);
    }

    public PotLauncherEntity(World world, Vec3d pos, EulerAngle angle) {
        this(world, pos);
        this.setAngles(angle);
    }

    public EulerAngle getAngles() {
        return this.dataTracker.get(ANGLES);
    }

    public void setAngles(EulerAngle angles) {
        this.dataTracker.set(ANGLES, angles);
    }

    public Box getPitchLeverHitBox() {
        return this.pitchLeverHitBox/*.offset(this.getX(), this.getY(), this.getZ())*/;
    }

    public Box getYawPullerHitbox() {
        return this.yawPullerHitbox/*.offset(this.getX(), this.getY(), this.getZ())*/;
    }

    private void updatedInteractionHitBoxes() {
        float yawInRad = (float) Math.toRadians(getAngles().getYaw());
        this.pitchLeverHitBox = EntityInteractionHitBox.calculateRotatedBox(this, DEFAULT_PITCH_HITBOX, yawInRad);
        this.yawPullerHitbox = EntityInteractionHitBox.calculateRotatedBox(this, DEFAULT_YAW_HITBOX, yawInRad);
    }

    @Override
    public void tick() {
        super.tick();
        updatedInteractionHitBoxes();
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
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
    }

    @Override
    public boolean isCollidable() {
        return true;
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
