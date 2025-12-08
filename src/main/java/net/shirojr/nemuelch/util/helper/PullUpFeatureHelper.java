package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import org.jetbrains.annotations.Nullable;

public class PullUpFeatureHelper {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPullUp(@Nullable PlayerEntity source, @Nullable Entity target) {
        if (source == null || !(target instanceof LivingEntity targetEntity)) return false;
        if (source.isSneaking() || source.isSpectator()) return false;
        if (!source.getMainHandStack().isEmpty()) return false;
        MiscEntityComponent component = MiscEntityComponent.get(source);
        if (component.isPullUpOnCooldown()) return false;
        if (source.isOnGround() || source.fallDistance > 0) return false;
        if (targetEntity.hurtTime > 0) return false;
        double sourceSize = source.getBoundingBox().getAverageSideLength();
        double targetSize = target.getBoundingBox().getAverageSideLength();
        return sourceSize >= targetSize;
    }

    public static void applyPullUp(PlayerEntity source, Entity targetEntity) {
        Vec3d pullForce = source.getPos().subtract(targetEntity.getPos()).normalize().multiply(0.5);
        pullForce = new Vec3d(pullForce.x, pullForce.y, pullForce.z);
        targetEntity.addVelocity(pullForce);
        targetEntity.velocityModified = true;
        targetEntity.velocityDirty = true;
        MiscEntityComponent component = MiscEntityComponent.get(source);
        if (!source.getWorld().isClient()) {
            component.setPullUpCooldown(40);
        }
    }
}
