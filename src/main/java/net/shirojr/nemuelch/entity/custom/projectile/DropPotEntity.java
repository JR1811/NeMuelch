package net.shirojr.nemuelch.entity.custom.projectile;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.network.NeMuelchS2CPacketHandler;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DropPotEntity extends ProjectileEntity {
    public static final int RENDER_DISTANCE = 300;
    public static final float FALLING_ACCELERATION = 0.04f;


    @Nullable
    private UUID userUuid;
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(DropPotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private double initialDropHeight;

    public DropPotEntity(World world) {
        super(NeMuelchEntities.DROP_POT, world);
    }

    public DropPotEntity(World world, @NotNull Entity user) {
        this(world);
        this.userUuid = user.getUuid();

        this.setVelocity(user.getVelocity());
        this.velocityDirty = true;
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, user.getBlockPos(), NeMuelchSounds.POT_RELEASE, SoundCategory.PLAYERS, 5f, 1f);
        }
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d potVelocity = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        double d = this.getX() + potVelocity.x;
        double e = this.getY() + potVelocity.y;
        double f = this.getZ() + potVelocity.z;
        this.updateRotation();

        Entity user = getUser(this.getWorld());

        double distance = user == null ? -1 : user.getPos().distanceTo(this.getPos());
        if ((distance > 5.0 || distance == -1) && !this.hasNoGravity()) {
            this.setVelocity(potVelocity.multiply(0.99F));
            if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -FALLING_ACCELERATION, 0.0));
                this.setPosition(d, e, f);
                this.velocityDirty = true;
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.world instanceof ServerWorld serverWorld) {
            NeMuelch.devLogger(String.valueOf(this.getVelocity().length()));
            onSmashed(serverWorld, true);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.world instanceof ServerWorld serverWorld) {
            NeMuelch.devLogger(String.valueOf(this.getVelocity().length()));
            onSmashed(serverWorld, true);
        }
        this.discard();
    }

    private void onSmashed(ServerWorld world, boolean shouldBreak) {
        SoundEvent landingSound = shouldBreak ? NeMuelchSounds.POT_HIT : NeMuelchSounds.POT_LAND;
        int particleAmount = shouldBreak ? 20 : 5;

        world.playSound(null, this.getBlockPos(), landingSound, SoundCategory.BLOCKS, 3f, 1f);
        for (int i = 0; i < particleAmount; i++) {
            world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getPos().getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    this.getPos().getY() + random.nextDouble(),
                    this.getPos().getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    1,
                    0.0, 0.07, 0.0,
                    0.2
            );
        }
        if (shouldBreak) this.discard();

    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(SoundInstanceHelper.DROP_POT.getIdentifier());
        buf.writeVarInt(this.getId());
        ServerPlayNetworking.send(player, NeMuelchS2CPacketHandler.START_SOUND_INSTANCE_CHANNEL, buf);
    }

    @Nullable
    public Entity getUser(World world) {
        if (!(world instanceof ServerWorld serverWorld) || this.userUuid == null) return null;
        return serverWorld.getEntity(this.userUuid);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }


    @Override
    public boolean collidesWith(Entity other) {
        return super.collidesWith(other);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("uuid")) {
            this.userUuid = nbt.getUuid("uuid");
        }
    }
}
