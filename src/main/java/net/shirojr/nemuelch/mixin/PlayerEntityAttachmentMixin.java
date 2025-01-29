package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.util.wrapper.Attachable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttachmentMixin extends LivingEntity implements Attachable {
    @Unique
    private static final TrackedData<Optional<UUID>> ATTACHED =
            DataTracker.registerData(PlayerEntityAttachmentMixin.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected PlayerEntityAttachmentMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    // ------------- Movement -------------

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void applyMovementRestriction(CallbackInfo ci) {
        if (nemuelch$getAttachedEntity().isEmpty()) return;
        if (world instanceof ServerWorld serverWorld) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            Entity other = serverWorld.getEntity(nemuelch$getAttachedEntity().get());
            if (other == null) {
                nemuelch$snap(serverWorld, null);
                return;
            }

            double sqDistance = player.squaredDistanceTo(other);
            double sqMaxDistance = PotLauncherEntity.LEASH_RELEASE_DISTANCE * PotLauncherEntity.LEASH_RELEASE_DISTANCE;
            if (sqDistance < sqMaxDistance * 0.25) return;
            else if (sqDistance > sqMaxDistance) {
                nemuelch$snap(serverWorld, other.getUuid());
                if (other instanceof Attachable otherAttachable) {
                    otherAttachable.nemuelch$snap(serverWorld, player.getUuid());
                }
            }

            double normalizedDistance = MathHelper.clamp(sqDistance / sqMaxDistance, 0, 1);
            double resistance = PotLauncherEntity.LEASH_RESISTANCE_FACTOR * (normalizedDistance * normalizedDistance);

            Vec3d direction = other.getPos().subtract(player.getPos());
            if (direction.length() <= 0) return;
            Vec3d normalizedDirection = direction.normalize();
            double strength = 0.1;

            player.addVelocity(
                    normalizedDirection.getX() * resistance * strength,
                    normalizedDirection.getY() * resistance * strength,
                    normalizedDirection.getZ() * resistance * strength
            );
            player.velocityModified = true;
        }
    }

    // ------------- Data Handling -------------

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addExtraDataTrackers(CallbackInfo ci) {
        dataTracker.startTracking(ATTACHED, Optional.empty());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readExtraNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.containsUuid("attached")) {
            nemuelch$setAttachedEntity(nbt.getUuid("attached"));
        } else {
            nemuelch$setAttachedEntity(null);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeExtraNbt(NbtCompound nbt, CallbackInfo ci) {
        nemuelch$getAttachedEntity().ifPresentOrElse(attachedEntity -> nbt.putUuid("attached", attachedEntity),
                () -> nbt.remove("attached"));
    }

    @Override
    public Optional<UUID> nemuelch$getAttachedEntity() {
        return dataTracker.get(ATTACHED);
    }

    @Override
    public void nemuelch$setAttachedEntity(@Nullable UUID entity) {
        dataTracker.set(ATTACHED, Optional.ofNullable(entity));
    }

    @Override
    public UUID nemuelch$getUuid() {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return player.getUuid();
    }
}
