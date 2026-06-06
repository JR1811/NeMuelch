package net.shirojr.nemuelch.util.helper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.client.NeMuelchCache;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;

public class PullUpFeatureHelper {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPullUp(@Nullable PlayerEntity source, @Nullable Entity target) {
        if (source == null || !(target instanceof LivingEntity targetEntity)) return false;
        if (!source.isSneaking() || source.isSpectator() || target.isSpectator()) return false;
        if (!source.getMainHandStack().isEmpty()) return false;
        if (!target.isInPose(EntityPose.STANDING)) return false;
        MiscEntityComponent component = MiscEntityComponent.get(source);
        if (component.isPullUpOnCooldown()) return false;
        if (!FabricLoader.getInstance().isDevelopmentEnvironment() && !source.isCreative()) {
            if (targetEntity.isOnGround() || targetEntity.fallDistance > 0) return false;
            if (targetEntity.hurtTime > 0) return false;
        }
        if (source.isCreative()) {
            return true;
        }
        if (source.getEyeY() < targetEntity.getEyeY()) return false;
        double sourceSize = source.getBoundingBox().getAverageSideLength();
        double targetSize = target.getBoundingBox().getAverageSideLength();
        return sourceSize >= targetSize;
    }

    public static void applyPullUp(PlayerEntity source, Entity targetEntity) {
        if (targetEntity instanceof PathAwareEntity pathAware) pathAware.getNavigation().stop();

        Vec3d pullForce = source.getPos().subtract(targetEntity.getPos()).normalize().multiply(0.5);

        double verticalStrength = source.getWorld().isClient() ?
                NeMuelchCache.pullUpVertStrength :
                source.getWorld().getGameRules().get(NemuelchGameRules.PULL_UP_VERT_STRENGTH).get();

        pullForce = new Vec3d(pullForce.x, pullForce.y + verticalStrength, pullForce.z);
        targetEntity.addVelocity(pullForce);
        targetEntity.velocityModified = true;
        targetEntity.velocityDirty = true;
        MiscEntityComponent component = MiscEntityComponent.get(source);
        if (source.getWorld() instanceof ServerWorld serverWorld) {
            component.setPullUpCooldown(40);
            Vec3d posBetween = source.getPos().lerp(targetEntity.getPos(), 0.5);
            serverWorld.playSound(null, posBetween.x, posBetween.y, posBetween.z, NeMuelchSounds.PULL_UP, SoundCategory.PLAYERS, 2f, 1f);
        }
    }
}
