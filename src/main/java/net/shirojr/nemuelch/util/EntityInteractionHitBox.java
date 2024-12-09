package net.shirojr.nemuelch.util;

import net.minecraft.util.math.Box;

public record EntityInteractionHitBox(String name, Box box, int color) {
    public static Box calculateRotatedBox(Box baseBox, float yawInRad) {
        double localCenterX = (baseBox.minX + baseBox.maxX) / 2;
        double localCenterZ = (baseBox.minZ + baseBox.maxZ) / 2;

        double rotatedOffsetX = localCenterX * Math.cos(yawInRad) - localCenterZ * Math.sin(yawInRad);
        double rotatedOffsetZ = localCenterX * Math.sin(yawInRad) + localCenterZ * Math.cos(yawInRad);

        double halfWidthX = (baseBox.maxX - baseBox.minX) / 2;
        double halfWidthZ = (baseBox.maxZ - baseBox.minZ) / 2;

        double minX = rotatedOffsetX - halfWidthX;
        double minZ = rotatedOffsetZ - halfWidthZ;
        double maxX = rotatedOffsetX + halfWidthX;
        double maxZ = rotatedOffsetZ + halfWidthZ;

        return new Box(minX, baseBox.minY, minZ, maxX, baseBox.maxY, maxZ);
    }
}
