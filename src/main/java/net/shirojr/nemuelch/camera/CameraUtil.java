package net.shirojr.nemuelch.camera;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class CameraUtil {
    public static boolean isCrosshairOver(Vec3d entry, Camera camera, float margin) {
        Vec3d camPos = camera.getPos();
        Vec3d toEntry = entry.subtract(camPos).normalize();
        Vec3d lookVec = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());

        double dot = lookVec.dotProduct(toEntry);
        double angleRad = Math.acos(MathHelper.clamp(dot, -1.0, 1.0));
        double angleDeg = Math.toDegrees(angleRad);

        return angleDeg < margin;
    }

    public static boolean hasObstruction(World world, Vec3d camPos, Vec3d notePos, Entity excludedFromRaycast) {
        double distance = camPos.distanceTo(notePos);
        RaycastContext context = new RaycastContext(camPos, notePos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, excludedFromRaycast);
        BlockHitResult hit = world.raycast(context);
        return hit.getType() != HitResult.Type.MISS && hit.getPos().distanceTo(camPos) < distance - 0.1;
    }
}
