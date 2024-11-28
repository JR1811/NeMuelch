package net.shirojr.nemuelch.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public record EntityInteractionHitBox(String name, Box box, int color) {
    public static Box calculateRotatedBox(Entity entity, Box baseBox, float yawInRad) {
        double centerX = entity.getX();
        double centerZ = entity.getZ();

        double offsetX = (baseBox.minX + baseBox.maxX) / 2;
        double offsetZ = (baseBox.minZ + baseBox.maxZ) / 2;

        double rotatedX = offsetX * Math.cos(yawInRad) - offsetZ * Math.sin(yawInRad);
        double rotatedZ = offsetX * Math.sin(yawInRad) + offsetZ * Math.cos(yawInRad);

        return baseBox.offset(centerX + rotatedX, entity.getY(), centerZ + rotatedZ);
    }
}
