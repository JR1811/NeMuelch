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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.util.EntityInteractionHitBox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class PotLauncherEntity extends Entity {
    public static final float HEIGHT = 2.4f;
    public static final float WIDTH = 2.2f;

    private static final EulerAngle DEFAULT_ANGLES = new EulerAngle(1.0F, 0.0F, 0.0F);
    private static final TrackedData<EulerAngle> ANGLES = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private final HashMap<InteractionHitBox, Box> interactionBoxes = new HashMap<>();

    private int activationTicks;


    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
        this.interactionBoxes.put(InteractionHitBox.PITCH_LEVER, InteractionHitBox.PITCH_LEVER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.YAW_PULLER, InteractionHitBox.YAW_PULLER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.LOADING_AREA, InteractionHitBox.LOADING_AREA.getLocalSpace());
        activationTicks = 0;
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
        //from 320° to 5.5°
        if (pitch < 0) {
            pitch = 360 + pitch;
        }
        if (pitch > 5 && pitch < 320) {
            pitch = this.getAngles().getPitch();
        }
        this.setAngles(new EulerAngle(pitch, yaw, DEFAULT_ANGLES.getRoll()));
        NeMuelch.devLogger("pitch: " + pitch + " | yaw: " + yaw);
    }

    public void setAngles(EulerAngle angles) {
        this.dataTracker.set(ANGLES, angles);
    }

    public int getActivationTicks() {
        return activationTicks;
    }

    public void setActivationTicks(int activationTicks) {
        this.activationTicks = activationTicks;
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
    public void updatePassengerPosition(Entity passenger) {
        if (passenger instanceof PlayerEntity player) {
            player.setYaw(this.getAngles().getYaw());
            player.setBodyYaw(this.getAngles().getYaw());
        }
        super.updatePassengerPosition(passenger);
    }

    @Override
    public void tick() {
        super.tick();
        this.updatedInteractionHitBoxes();
        this.setActivationTicks(this.activationTicks + 1);

/*        if (!this.getWorld().isClient()) {
            this.setAngles(this.getAngles().getPitch(), this.getAngles().getYaw() + 5f);
        }*/
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

        if (!this.getWorld().isClient()) {
            closestInteraction.getKey().onHit(this, player.isSneaking() ? 1.0 : -1.0);
        }
        if (closestInteraction.getKey().equals(InteractionHitBox.LOADING_AREA)) {
            return startRiding(player);
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult startRiding(PlayerEntity player) {
        if (!this.world.isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
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
        PITCH_LEVER("pitch_lever", 1.2, 0.25, 0.25,
                new Vec3d(0.9f, 0.65f, 0.5f),
                new Vec3f(0.988235294f, 0.011764706f, 0.925490196f),
                (entity, delta) -> {
                    int change = delta > 0 ? 5 : -5;
                    entity.setAngles(entity.getAngles().getPitch() + change, entity.getAngles().getYaw());
                    NeMuelch.devLogger("interacted with PITCH | new Pitch: " + entity.getAngles().getPitch());
                    playSound(entity, SoundEvents.ITEM_AXE_STRIP, 0.9f, 1.0f);
                }),
        YAW_PULLER("yaw_puller", 0.6, 0.25, 0.9,
                new Vec3d(-0.9f, 0.05f, 0.5f),
                new Vec3f(0.71372549f, 0.988235294f, 0.011764706f),
                (entity, delta) -> {
                    int change = delta > 0 ? 5 : -5;
                    entity.setAngles(entity.getAngles().getPitch(), entity.getAngles().getYaw() + change);
                    NeMuelch.devLogger("interacted with YAW | new Yaw: " + entity.getAngles().getYaw());
                    playSound(entity, SoundEvents.BLOCK_WOOD_STEP, 0.8f, 1.1f);
                }),
        LOADING_AREA("loading_area", 0.5, 0.9, 1.5,
                new Vec3d(0.0f, 0.5f, 0.25f),
                new Vec3f(0.658823529f, 0.529411765f, 0.870588235f),
                (entity, delta) -> {
                    //TODO: implement loading interaction with player or projectiles
                    NeMuelch.devLogger("interacted with LOADING AREA");
                });

        private final String name;
        private final Box localSpace;
        private final Vec3f debugColor;
        private final BiConsumer<PotLauncherEntity, Double> action;

        InteractionHitBox(String name, double minY, double width, double height, Vec3d offset,
                          Vec3f debugColor, BiConsumer<PotLauncherEntity, Double> action) {
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

        public static Optional<InteractionHitBox> byName(String name) {
            return Arrays.stream(values()).filter(box -> box.asString().equals(name)).findFirst();
        }

        public Box getLocalSpace() {
            return localSpace;
        }

        public Vec3f getDebugColor() {
            return debugColor;
        }

        public void onHit(PotLauncherEntity entity, Double delta) {
            this.action.accept(entity, delta);
        }

        private static void playSound(PotLauncherEntity entity, SoundEvent sound, float minPitch, float maxPitch) {
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                float randomPitch = MathHelper.lerp(serverWorld.getRandom().nextFloat(), minPitch, maxPitch);
                serverWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        sound, SoundCategory.NEUTRAL, 2f, randomPitch);
            }
        }
    }
}
