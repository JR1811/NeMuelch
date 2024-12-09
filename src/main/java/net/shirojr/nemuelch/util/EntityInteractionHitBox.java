package net.shirojr.nemuelch.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public record EntityInteractionHitBox(String name, Box box, int color) {
    public static Box calculateRotatedBox(Entity entity, Box baseBox, float yawInRad) {
        double localCenterX = (baseBox.minX + baseBox.maxX) / 2;
        double localCenterZ = (baseBox.minZ + baseBox.maxZ) / 2;

        double offsetX = localCenterX - entity.getX();
        double offsetZ = localCenterZ - entity.getZ();

        double rotatedOffsetX = offsetX * Math.cos(yawInRad) - offsetZ * Math.sin(yawInRad);
        double rotatedOffsetZ = offsetX * Math.sin(yawInRad) + offsetZ * Math.cos(yawInRad);

        double rotatedCenterX = entity.getX() + rotatedOffsetX;
        double rotatedCenterZ = entity.getZ() + rotatedOffsetZ;

        double halfWidthX = (baseBox.maxX - baseBox.minX) / 2;
        double halfWidthZ = (baseBox.maxZ - baseBox.minZ) / 2;

        double minX = rotatedCenterX - halfWidthX;
        double minZ = rotatedCenterZ - halfWidthZ;
        double maxX = rotatedCenterX + halfWidthX;
        double maxZ = rotatedCenterZ + halfWidthZ;

        return new Box(minX, baseBox.minY, minZ, maxX, baseBox.maxY, maxZ);
    }
}
