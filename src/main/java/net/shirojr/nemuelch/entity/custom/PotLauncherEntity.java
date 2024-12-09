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
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.util.EntityInteractionHitBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PotLauncherEntity extends Entity {
    public static final float HEIGHT = 2.4f;
    public static final float WIDTH = 2.2f;

    private static final EulerAngle DEFAULT_ANGLES = new EulerAngle(1.0F, 0.0F, 0.0F);
    private static final TrackedData<EulerAngle> ANGLES = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private final HashMap<InteractionHitBox, Box> interactionBoxes = new HashMap<>();


    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
        this.interactionBoxes.put(InteractionHitBox.PITCH_LEVER, InteractionHitBox.PITCH_LEVER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.YAW_PULLER, InteractionHitBox.YAW_PULLER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.LOADING_AREA, InteractionHitBox.LOADING_AREA.getLocalSpace());
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

    public HashMap<InteractionHitBox, Box> getInteractionBoxes() {
        return interactionBoxes;
    }

    private void updatedInteractionHitBoxes() {
        float yawInRad = (float) Math.toRadians(getAngles().getYaw());
        for (var entry : this.interactionBoxes.entrySet()) {
            entry.setValue(EntityInteractionHitBox.calculateRotatedBox(entry.getKey().getLocalSpace(), yawInRad));
        }
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

        Map.Entry<InteractionHitBox, Box> closestInteraction = null;
        double closestRange = Double.MAX_VALUE;
        for (var entry : this.interactionBoxes.entrySet()) {
            Box worldSpaceBox = entry.getValue().offset(this.getPos());
            if (worldSpaceBox.raycast(start, end).isPresent()) {
                double sqDistance = worldSpaceBox.raycast(start, end).get().squaredDistanceTo(start);
                if (closestInteraction == null || closestRange > sqDistance) {
                    closestInteraction = entry;
                    closestRange = sqDistance;
                }
            }
        }

        if (closestInteraction == null) {
            return super.interact(player, hand);
        }

        closestInteraction.getKey().onHit(this, player);
        return ActionResult.SUCCESS;
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

    public enum InteractionHitBox implements StringIdentifiable {
        PITCH_LEVER("pitch_lever", 1.2, 0.25, 0.25, new Vec3d(0.9f, 0.65f, 0.5f),
                new Vec3f(0.988235294f, 0.011764706f, 0.925490196f),
                (entity, player) -> {
                    int change = player.isSneaking() ? -10 : 10;
                    entity.setAngles(entity.getAngles().getPitch() + change, entity.getAngles().getYaw());
                    NeMuelch.devLogger("interacted with PITCH | new Pitch: " + entity.getAngles().getYaw());
                }),
        YAW_PULLER("yaw_puller", 0.6, 0.25, 0.9, new Vec3d(-0.9f, 0.05f, 0.5f),
                new Vec3f(0.71372549f, 0.988235294f, 0.011764706f),
                (entity, player) -> {
                    int change = player.isSneaking() ? -5 : 5;
                    entity.setAngles(entity.getAngles().getPitch(), entity.getAngles().getYaw() + change);
                    NeMuelch.devLogger("interacted with YAW | new Yaw: " + entity.getAngles().getYaw());
                }),
        LOADING_AREA("loading_area", 0.5, 0.8, 0.5, new Vec3d(0.0f, 0.0f, -2.0f),
                new Vec3f(0.5f, 0.2f, 0.7f),
                (entity, player) -> {
                    //TODO: implement loading interaction with player or projectiles
                    NeMuelch.devLogger("interacted with LOADING AREA");
                });

        private final String name;
        private final Box localSpace;
        private final Vec3f debugColor;
        private final BiConsumer<PotLauncherEntity, PlayerEntity> action;

        InteractionHitBox(String name, double minY, double width, double height, Vec3d offset,
                          Vec3f debugColor, BiConsumer<PotLauncherEntity, PlayerEntity> action) {
            this.name = name;
            this.localSpace = new Box((-width / 2), minY, (-width / 2), (width / 2), minY + height, (width / 2)).offset(offset);
            this.debugColor = debugColor;
            this.action = action;
        }

        @Override
        public String asString() {
            return name;
        }

        public String getName() {
            return asString();
        }

        public Box getLocalSpace() {
            return localSpace;
        }

        public Vec3f getDebugColor() {
            return debugColor;
        }

        public void onHit(PotLauncherEntity entity, PlayerEntity player) {
            this.action.accept(entity, player);
        }
    }
}
