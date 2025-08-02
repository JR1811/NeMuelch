package net.shirojr.nemuelch.camera;

import net.minecraft.util.math.Vec3d;

public interface CameraShake {
    Displacement getCurrentDisplacement();

    boolean isFinished();

    void initialize(Displacement displacement);

}
