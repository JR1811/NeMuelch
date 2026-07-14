package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

public class RaycastHelper {
    private RaycastHelper() {

    }

    public static Optional<EntityHitResult> raycastEntities(LivingEntity user, Vec3d start, Vec3d end,
                                                            Predicate<Entity> predicate, boolean checkBlockObstruction) {
        World world = user.getWorld();
        Box searchBox = user.getBoundingBox().stretch(end.subtract(start)).expand(1.0);
        double rangeSq = start.squaredDistanceTo(end);
        BlockHitResult blockRaycast = null;
        if (checkBlockObstruction) {
            blockRaycast = world.raycast(
                    new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, user)
            );
            if (blockRaycast.getType() != HitResult.Type.BLOCK) {
                blockRaycast = null;
            }
        }
        EntityHitResult entityRaycast = ProjectileUtil.raycast(user, start, end, searchBox, predicate, rangeSq);
        if (blockRaycast == null) return Optional.ofNullable(entityRaycast);
        if (entityRaycast == null) return Optional.empty();
        Vec3d blockHitPos = blockRaycast.getPos();
        Vec3d entityHitPos = entityRaycast.getPos();
        if (blockHitPos.squaredDistanceTo(start) < entityHitPos.squaredDistanceTo(start)) {
            return Optional.empty();
        }
        return Optional.of(entityRaycast);
    }
}
