package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.util.EntityInteractionHitBox;

public class PotLauncherEntity extends Entity {
    public static final float HEIGHT = 2.4f;
    public static final float WIDTH = 2.2f;

    private static final double yawHitBoxWidth = 0.25, pitchHitBoxWidth = 0.26;
    private static final Vec3d PITCH_HITBOX_OFFSET = new Vec3d(0.9f, 0.65f, 0.5f);
    private static final Vec3d YAW_HITBOX_OFFSET = new Vec3d(-0.9f, 0.05f, 0.5f);

    private static final Box DEFAULT_PITCH_HITBOX = new Box(
            -(pitchHitBoxWidth / 2), 1.2, -(pitchHitBoxWidth / 2),
            (pitchHitBoxWidth / 2), 1.45, (pitchHitBoxWidth / 2)
    ).offset(PITCH_HITBOX_OFFSET);

    public static final Box DEFAULT_YAW_HITBOX = new Box(
            -(yawHitBoxWidth / 2), 0.6, -(yawHitBoxWidth / 2),
            (yawHitBoxWidth / 2), 1.5, (yawHitBoxWidth / 2)
    ).offset(YAW_HITBOX_OFFSET);

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

    public PotLauncherEntity(World world, Vec3d pos, float pitch, float yaw) {
        this(world, pos);
        this.setAngles(pitch, yaw);
    }

    public EulerAngle getAngles() {
        return this.dataTracker.get(ANGLES);
    }

    public void setAngles(float pitch, float yaw) {
        this.setAngles(new EulerAngle(pitch, yaw, DEFAULT_ANGLES.getPitch()));
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
        this.pitchLeverHitBox = EntityInteractionHitBox.calculateRotatedBox(DEFAULT_PITCH_HITBOX, yawInRad);
        this.yawPullerHitbox = EntityInteractionHitBox.calculateRotatedBox(DEFAULT_YAW_HITBOX, yawInRad);
    }

    @Override
    public void tick() {
        super.tick();
        updatedInteractionHitBoxes();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector().normalize().multiply(5.0);
        Vec3d end = start.add(direction);
        Box worldSpaceYawPuller = this.yawPullerHitbox.offset(this.getX(), this.getY(), this.getZ());
        Box worldSpacePitchLever = this.pitchLeverHitBox.offset(this.getX(), this.getY(), this.getZ());

        int pitchChange = 0;
        int yawChange = 0;

        if (worldSpaceYawPuller.raycast(start, end).isPresent() && worldSpacePitchLever.raycast(start, end).isPresent()) {
            NeMuelch.devLogger("used both");    //FIXME: Future ShiroJR will probably take care of that... hopefully
            return super.interact(player, hand);
        }

        if (worldSpaceYawPuller.raycast(start, end).isPresent()) {
            if (player.isSneaking()) yawChange -= 5;
            else yawChange += 5;
            NeMuelch.devLogger("interacted with YAW | new Yaw: " + this.getAngles().getYaw());
        }
        if (worldSpacePitchLever.raycast(start, end).isPresent()) {
            if (player.isSneaking()) pitchChange -= 2;
            else pitchChange += 2;
            NeMuelch.devLogger("interacted with PITCH | new Pitch: " + this.getAngles().getPitch());
        }
        this.setAngles(this.getAngles().getPitch() + pitchChange, this.getAngles().getYaw() + yawChange);
        if (pitchChange != 0 || yawChange != 0) {
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
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
    public boolean collides() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return super.handleAttack(attacker);
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
