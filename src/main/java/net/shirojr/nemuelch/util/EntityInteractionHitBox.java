package net.shirojr.nemuelch.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public record EntityInteractionHitBox(String name, Box box, int color) {
    public static Box calculateRotatedBox(Entity entity, Box baseBox, float yawInRad) {
        double centerX = entity.getX();
        double centerZ = entity.getZ();
        double localCenterX = (baseBox.minX + baseBox.maxX) / 2;
        double localCenterZ = (baseBox.minZ + baseBox.maxZ) / 2;
        double rotatedLocalCenterX = localCenterX * Math.cos(yawInRad) - localCenterZ * Math.sin(yawInRad);
        double rotatedLocalCenterZ = localCenterX * Math.sin(yawInRad) + localCenterZ * Math.cos(yawInRad);

        double rotatedBoxCenterX = rotatedLocalCenterX - localCenterX;
        double rotatedBoxCenterZ = rotatedLocalCenterZ - localCenterZ;

        double newBoxOffsetX = rotatedBoxCenterX - localCenterX;
        double newBoxOffsetZ = rotatedBoxCenterZ - localCenterZ;

        return baseBox.offset(newBoxOffsetX, 0, newBoxOffsetZ);
    }
}
