package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.AttachableComponent;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.util.helper.AttachableHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttachmentMixin extends LivingEntity {
    protected PlayerEntityAttachmentMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void applyMovementRestriction(CallbackInfo ci) {
        AttachableComponent attachableComponent = AttachableComponent.get(this);
        if (attachableComponent.getAttachedEntity() == null) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        Entity other = attachableComponent.getAttachedEntity();
        if (other == null) {
            AttachableHelper.detachBoth(AttachableComponent.get(player), AttachableComponent.get(other));
            return;
        }

        double sqDistance = player.squaredDistanceTo(other);
        double sqMaxDistance = PotLauncherEntity.LEASH_RELEASE_DISTANCE * PotLauncherEntity.LEASH_RELEASE_DISTANCE;
        if (sqDistance < sqMaxDistance * 0.25) return;
        else if (sqDistance > sqMaxDistance) {
            if (getWorld() instanceof ServerWorld serverWorld) {
                attachableComponent.snap(serverWorld, other, (otherAfterSnap) -> {
                    if (!(otherAfterSnap instanceof PotLauncherEntity potLauncher) || serverWorld == null) return;
                    ItemScatterer.spawn(serverWorld,
                            potLauncher.getItemDropPosition().x,
                            potLauncher.getItemDropPosition().y,
                            potLauncher.getItemDropPosition().z,
                            Items.LEAD.getDefaultStack()
                    );
                    potLauncher.setActive(true);
                });
            }
        }

        double normalizedDistance = MathHelper.clamp(sqDistance / sqMaxDistance, 0, 1);
        double resistance = PotLauncherEntity.LEASH_RESISTANCE_FACTOR * (normalizedDistance * normalizedDistance);

        Vec3d direction = other.getPos().subtract(player.getPos());
        if (direction.length() <= 0) return;
        Vec3d normalizedDirection = direction.normalize();
        double strength = 0.1;
        if (player.isLogicalSideForUpdatingMovement()) {
            player.addVelocity(
                    normalizedDirection.getX() * resistance * strength,
                    normalizedDirection.getY() * resistance * strength,
                    normalizedDirection.getZ() * resistance * strength
            );
            player.velocityModified = true;
        }
    }
}
