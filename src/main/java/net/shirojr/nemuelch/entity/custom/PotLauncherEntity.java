package net.shirojr.nemuelch.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.AttachableComponent;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import net.shirojr.nemuelch.util.EntityInteractionHitBox;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.AttachableHelper;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class PotLauncherEntity extends Entity {
    public static final float WIDTH = 2.2f;
    public static final float HEIGHT = 2.4f;

    public static final double LEASH_RELEASE_DISTANCE = 5.0;
    public static final double LEASH_RESISTANCE_FACTOR = 0.5;

    public static final int ACTIVATION_DURATION = 20;

    private static final EulerAngle DEFAULT_ANGLES = new EulerAngle(1.0F, 0.0F, 0.0F);
    private static final TrackedData<EulerAngle> ANGLES = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<ItemStack> POT_SLOT = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Optional<UUID>> LEASH_HOLDER = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    @Nullable
    private ItemStack attachedLeadItem;

    private final HashMap<InteractionHitBox, Box> interactionBoxes = new HashMap<>();

    private int activationTicks;
    private int dismountCooldownTicks;


    public PotLauncherEntity(EntityType<PotLauncherEntity> dropPotEntityEntityType, World world) {
        super(dropPotEntityEntityType, world);
        this.interactionBoxes.put(InteractionHitBox.PITCH_LEVER, InteractionHitBox.PITCH_LEVER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.YAW_PULLER, InteractionHitBox.YAW_PULLER.getLocalSpace());
        this.interactionBoxes.put(InteractionHitBox.LOADING_AREA, InteractionHitBox.LOADING_AREA.getLocalSpace());
        this.activationTicks = -1;
        this.dismountCooldownTicks = -1;
    }

    public PotLauncherEntity(World world, Vec3d pos) {
        this(NeMuelchEntities.POT_LAUNCHER, world);
        this.setPosition(pos);
    }

    public PotLauncherEntity(World world, Vec3d pos, float pitch, float yaw) {
        this(world, pos);
        this.setAngles(pitch, yaw);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ANGLES, DEFAULT_ANGLES);
        this.dataTracker.startTracking(POT_SLOT, ItemStack.EMPTY);
        this.dataTracker.startTracking(LEASH_HOLDER, Optional.empty());
    }

    public EulerAngle getAngles() {
        return this.dataTracker.get(ANGLES);
    }

    public void setAngles(EulerAngle angles) {
        this.dataTracker.set(ANGLES, angles);
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
        LoggerUtil.devLogger("pitch: " + pitch + " | yaw: " + yaw);
    }

    public ItemStack getPotSlot() {
        return this.dataTracker.get(POT_SLOT);
    }

    public boolean setPotSlot(ItemStack stack) {
        if (!(stack.getItem() instanceof DropPotBlockItem)) return false;
        this.dataTracker.set(POT_SLOT, stack.copy());
        return true;
    }

    public void clearPotSlot() {
        this.dataTracker.set(POT_SLOT, ItemStack.EMPTY);
    }

    @Nullable
    public ItemStack getAttachedLeadItem() {
        return attachedLeadItem;
    }

    public void setAttachedLeadItem(@Nullable ItemStack attachedLeadItem) {
        this.attachedLeadItem = attachedLeadItem;
    }

    public int getActivationTicks() {
        return activationTicks;
    }

    public void setActive(boolean active) {
        this.activationTicks = active ? 0 : -1;
        if (getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(this)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(this.getId());
                buf.writeBoolean(active);
                ServerPlayNetworking.send(serverPlayerEntity, NetworkIdentifiers.POT_LAUNCHER_ACTIVATED, buf);
            }
            if (active) {
                serverWorld.playSound(null, this.getBlockPos(), NeMuelchSounds.LAUNCHER_LAUNCH, SoundCategory.BLOCKS);
            }
        }
    }

    public boolean isActivated() {
        return this.activationTicks != -1;
    }

    public void setActivationTicks(int activationTicks) {
        this.activationTicks = activationTicks;
    }

    public void onFinishedActivation() {
        this.spawnAndThrowEntity();
        this.setActive(false);
    }

    public int getDismountCooldownTicks() {
        return dismountCooldownTicks;
    }

    public void setDismountCooldownTicks(int dismountCooldownTicks) {
        this.dismountCooldownTicks = dismountCooldownTicks;
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
    public void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (passenger instanceof PlayerEntity player) {
            player.setYaw(this.getAngles().getYaw());
            player.setBodyYaw(this.getAngles().getYaw());
        }
        super.updatePassengerPosition(passenger, positionUpdater);

        if (!this.hasPassenger(passenger)) return;

        double pitchInRad = Math.toRadians(this.getAngles().getPitch());
        double yawInRad = Math.toRadians(this.getAngles().getYaw());
        float normalizedPosition = (float) this.getActivationTicks() / PotLauncherEntity.ACTIVATION_DURATION;
        double distance = MathHelper.lerp(normalizedPosition * normalizedPosition * normalizedPosition, 1.5f, 0f);
        Vec3d offset = new Vec3d(0, 0.7, 0);

        double x = distance * Math.cos(pitchInRad) * Math.sin(yawInRad);
        double y = distance * Math.sin(pitchInRad);
        double z = -distance * Math.cos(pitchInRad) * Math.cos(yawInRad);

        Vec3d newPosition = new Vec3d(x, y, z).add(this.getPos()).add(offset);
        positionUpdater.accept(passenger, newPosition.x, newPosition.y, newPosition.z);
    }

    @Override
    public void tick() {
        super.tick();
        this.updatedInteractionHitBoxes();

        if (this.getActivationTicks() >= 0 && this.getActivationTicks() <= ACTIVATION_DURATION) {
            this.setActivationTicks(this.getActivationTicks() + 1);
        }
        if (this.getDismountCooldownTicks() >= 0) {
            this.setDismountCooldownTicks(this.getDismountCooldownTicks() + 1);
        }

        if (this.getActivationTicks() >= ACTIVATION_DURATION) {
            this.onFinishedActivation();
        }
        if (this.getDismountCooldownTicks() > 60) {
            this.setDismountCooldownTicks(-1);
        }

        Box entityBox = this.getBoundingBox().expand(5.0);
        List<Entity> entitiesInRange = this.getWorld().getOtherEntities(this, entityBox);
        Entity closestEntity = getClosestEntity(entitiesInRange);

        if (closestEntity == null) return;
        if (this.getDismountCooldownTicks() == -1) {
            if (closestEntity instanceof PlayerEntity closestPlayer && isOnTop(this, closestPlayer, 0.5)) {
                if (!this.getWorld().isClient()) {
                    startRiding(closestPlayer);
                }
                this.setDismountCooldownTicks(0);
            }
            if (closestEntity instanceof ItemEntity itemEntity && isOnTop(this, itemEntity, 0.5)) {
                if (itemEntity.getStack().getItem() instanceof DropPotBlockItem) {
                    if (!this.getWorld().isClient()) {
                        this.setPotSlot(itemEntity.getStack().copy());
                        itemEntity.discard();
                    }
                    this.setDismountCooldownTicks(0);
                }
            }
        }

        if (this.getActivationTicks() == -1 && this.hasPassengers() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            this.setActive(true);
        }
    }

    @Nullable
    private Entity getClosestEntity(List<Entity> entities) {
        if (entities.isEmpty()) return null;
        Entity closestEntity = null;
        for (Entity entity : entities) {
            if (closestEntity == null) {
                closestEntity = entity;
                continue;
            }
            double distance = this.getPos().squaredDistanceTo(entity.getPos());
            double closestDistance = this.getPos().squaredDistanceTo(closestEntity.getPos());
            if (distance < closestDistance) {
                closestEntity = entity;
            }
        }
        return closestEntity;
    }

    @Override
    public Vec3d getLeashOffset() {
        return super.getLeashOffset().add(0, -0.5, 0);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        AttachableComponent attachableComponent = AttachableComponent.get(this);
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof LeadItem && attachableComponent.getAttachedEntity() == null) {
            if (getWorld() instanceof ServerWorld) {
                AttachableHelper.attachBoth(attachableComponent, AttachableComponent.get(player));
                this.setAttachedLeadItem(stack.copy());
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        } else if (attachableComponent.getAttachedEntity() != null && attachableComponent.getAttachedEntity().getUuid().equals(player.getUuid())) {
            if (getWorld() instanceof ServerWorld) {
                AttachableHelper.detachBoth(attachableComponent, AttachableComponent.get(player));
                if (this.getAttachedLeadItem() != null) {
                    ItemScatterer.spawn(getWorld(),
                            getItemDropPosition().getX(), getItemDropPosition().getY(), getItemDropPosition().getZ(),
                            this.getAttachedLeadItem());
                    this.setAttachedLeadItem(null);
                }
            }
            return ActionResult.SUCCESS;
        }

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
            closestInteraction.getKey().onHit(this, player.isSneaking() ? 1.0 : -1.0, 10f);
        }

        if (closestInteraction.getKey().equals(InteractionHitBox.LOADING_AREA)) {
            if (!this.hasProjectileOrEntityLoaded()) {
                if (stack.isEmpty()) {
                    return startRiding(player) ? ActionResult.SUCCESS : ActionResult.PASS;
                }
                if (stack.getItem() instanceof DropPotBlockItem) {
                    if (this.setPotSlot(stack)) {
                        if (player.isCreative()) {
                            this.setActive(true);
                        } else {
                            stack.decrement(1);
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    public boolean hasProjectileOrEntityLoaded() {
        return !this.getPotSlot().isEmpty() || !this.getPassengerList().isEmpty();
    }

    private boolean startRiding(PlayerEntity player) {
        if (this.hasPassengers()) return false;
        if (getWorld() instanceof ServerWorld serverWorld) {
            if (!this.getPotSlot().isEmpty()) {
                ItemScatterer.spawn(serverWorld,
                        this.getItemDropPosition().getX(), this.getItemDropPosition().getY(), this.getItemDropPosition().getZ(),
                        this.getPotSlot().copy());
                this.clearPotSlot();
            }
            return player.startRiding(this);
        }
        return true;
    }

    public void spawnAndThrowEntity() {
        double spawnDistance = 2.0;
        double pitchInRad = -Math.toRadians(this.getAngles().getPitch());
        double yawInRad = Math.toRadians(this.getAngles().getYaw());

        Vec3d launchPos = this.getPos().add(new Vec3d(
                spawnDistance * -Math.sin(yawInRad) * Math.cos(pitchInRad),
                spawnDistance * Math.sin(pitchInRad) + 2.0,
                spawnDistance * Math.cos(yawInRad) * Math.cos(pitchInRad)
        ));
        Vec3d direction = new Vec3d(
                -Math.cos(pitchInRad) * Math.sin(yawInRad),
                Math.sin(pitchInRad),
                Math.cos(pitchInRad) * Math.cos(yawInRad)
        );

        if (this.hasPassengers() && this.getPassengerList().get(0) instanceof PlayerEntity player) {
            player.stopRiding();
            if (player.isLogicalSideForUpdatingMovement()) {
                player.setPosition(launchPos);
                player.setVelocity(direction.multiply(3));
                player.velocityModified = true;
            }
            if (player.checkFallFlying()) {
                player.startFallFlying();
            }
        } else if (!this.getPotSlot().isEmpty()) {
            DropPotEntity potEntity = new DropPotEntity(this.getWorld(), launchPos, direction.multiply(2), this.getPotSlot().copy());
            this.getWorld().spawnEntity(potEntity);
            this.clearPotSlot();
        }
        this.dismountCooldownTicks = 0;
    }

    public Vec3d getItemDropPosition() {
        return this.getPos().add(0, HEIGHT, 0);
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 16.0;
        d *= 64.0 * getRenderDistanceMultiplier();
        return distance < d * d;
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
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandStack();
            if (stack.getItem() instanceof AxeItem) {
                if (livingEntity.getWorld() instanceof ServerWorld serverWorld) {
                    this.onBreak(serverWorld);
                }
                return true;
            }
        }
        return super.handleAttack(attacker);
    }

    public void onBreak(ServerWorld serverWorld) {
        serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.NEUTRAL, 1f, 1f);
        ItemScatterer.spawn(serverWorld,
                this.getItemDropPosition().getX(), this.getItemDropPosition().getY(), this.getItemDropPosition().getZ(),
                NeMuelchItems.POT_LAUNCHER.getDefaultStack());
        this.discard();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        NbtList anglesNbt = nbt.getList("angles", NbtElement.FLOAT_TYPE);
        this.setAngles(anglesNbt.isEmpty() ? DEFAULT_ANGLES : new EulerAngle(anglesNbt));
        this.setPotSlot(ItemStack.fromNbt(nbt.getCompound("pot_slot")));

        if (nbt.contains("leashItem")) {
            NbtCompound leashItemNbt = nbt.getCompound("leashItem");
            this.setAttachedLeadItem(ItemStack.fromNbt(leashItemNbt));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!DEFAULT_ANGLES.equals(this.getAngles())) nbt.put("angles", this.getAngles().toNbt());

        NbtCompound potSlotNbt = new NbtCompound();
        this.getPotSlot().writeNbt(potSlotNbt);
        nbt.put("loaded", potSlotNbt);

        if (this.getAttachedLeadItem() != null) {
            NbtCompound leashItemNbt = new NbtCompound();
            this.getAttachedLeadItem().writeNbt(leashItemNbt);
            nbt.put("leashItem", leashItemNbt);
        }
    }

    public static boolean isOnTop(Entity baseEntity, Entity topEntity, double verticalExpand) {
        if (baseEntity.hasPassenger(topEntity)) return false;
        Box entityBox = baseEntity.getBoundingBox();
        Vec3d topEntityPos = topEntity.getPos();
        boolean isVerticallyAligned = topEntityPos.y >= entityBox.maxY && topEntityPos.y <= entityBox.maxY + verticalExpand;
        boolean isHorizontallyAligned = topEntityPos.x >= entityBox.minX && topEntityPos.x <= entityBox.maxX &&
                topEntityPos.z >= entityBox.minZ && topEntityPos.z <= entityBox.maxZ;
        return isVerticallyAligned && isHorizontallyAligned;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (getWorld() instanceof ServerWorld) {
            AttachableComponent attachableComponent = AttachableComponent.get(this);
            AttachableHelper.detachBoth(attachableComponent, AttachableComponent.get(attachableComponent.getAttachedEntity()));
        }
    }

    public enum InteractionHitBox implements StringIdentifiable {
        PITCH_LEVER("pitch_lever", 1.2, 0.25, 0.25,
                new Vec3d(0.9f, 0.65f, 0.5f),
                new Vector3f(0.988235294f, 0.011764706f, 0.925490196f),
                (entity, delta, angleChange) -> {
                    float change = delta > 0 ? angleChange : -angleChange;
                    entity.setAngles(entity.getAngles().getPitch() + change, entity.getAngles().getYaw());
                    playSound(entity, SoundEvents.ITEM_AXE_STRIP, 0.9f, 1.0f);
                }, true),
        YAW_PULLER("yaw_puller", 0.6, 0.25, 0.9,
                new Vec3d(-0.9f, 0.05f, 0.5f),
                new Vector3f(0.71372549f, 0.988235294f, 0.011764706f),
                (entity, delta, angleChange) -> {
                    float change = delta > 0 ? angleChange : -angleChange;
                    entity.setAngles(entity.getAngles().getPitch(), entity.getAngles().getYaw() + change);
                    playSound(entity, SoundEvents.BLOCK_WOOD_STEP, 0.8f, 1.1f);
                }, true),
        LOADING_AREA("loading_area", 0.5, 0.9, 1.5,
                new Vec3d(0.0f, 0.5f, 0.25f),
                new Vector3f(0.658823529f, 0.529411765f, 0.870588235f),
                (entity, delta, angleChange) -> {

                }, false);

        private final String name;
        private final Box localSpace;
        private final Vector3f debugColor;
        private final TriConsumer<PotLauncherEntity, Double, Float> action;
        private final boolean scrollable;

        InteractionHitBox(String name, double minY, double width, double height, Vec3d offset,
                          Vector3f debugColor, TriConsumer<PotLauncherEntity, Double, Float> action, boolean scrollable) {
            this.name = name;
            this.localSpace = new Box((-width / 2), minY, (-width / 2), (width / 2), minY + height, (width / 2)).offset(offset);
            this.debugColor = debugColor;
            this.action = action;
            this.scrollable = scrollable;
        }

        @Override
        public String asString() {
            return name;
        }

        public String getName() {
            return asString();
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isScrollable() {
            return scrollable;
        }

        public static Optional<InteractionHitBox> byName(String name) {
            return Arrays.stream(values()).filter(box -> box.asString().equals(name)).findFirst();
        }

        public Box getLocalSpace() {
            return localSpace;
        }

        public Vector3f getDebugColor() {
            return debugColor;
        }

        public void onHit(PotLauncherEntity entity, Double delta) {
            this.onHit(entity, delta, 0f);
        }

        public void onHit(PotLauncherEntity entity, Double delta, float angleChange) {
            this.action.accept(entity, delta, angleChange);
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
