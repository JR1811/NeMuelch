package net.shirojr.nemuelch.entity.custom.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DropPotEntity extends ProjectileEntity {

    @Nullable
    private UUID userUuid;
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(DropPotEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public DropPotEntity(World world) {
        super(NeMuelchEntities.DROP_POT, world);
        this.noClip = true;
        this.setNoGravity(false);
    }

    public DropPotEntity(World world, @NotNull Entity user) {
        this(world);
        this.userUuid = user.getUuid();

        this.setVelocity(user.getVelocity());
        this.velocityDirty = true;

    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d vec3d = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        this.updateRotation();

        Entity user = getUser(this.getWorld());
        if (user != null) {
            double distance = user.getPos().distanceTo(this.getPos());
            if (distance > 5.0) {
                this.setVelocity(vec3d.multiply(0.99F));
                if (!this.hasNoGravity()) {
                    this.setVelocity(this.getVelocity().add(0.0, -0.06F, 0.0));
                    this.setPosition(d, e, f);
                    this.velocityDirty = true;
                }
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
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
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("uuid")) {
            this.userUuid = nbt.getUuid("uuid");
        }
    }
}
