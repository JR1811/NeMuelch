package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.entity.custom.LiftCounterWeightEntity;
import net.shirojr.nemuelch.entity.custom.LiftPlatformEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LiftSystemManager {
    public static final HashMap<UUID, LiftSystemManager> activeSystems = new HashMap<>();

    private final LiftPlatformEntity liftEntity;
    private final LiftCounterWeightEntity counterWeightEntity;
    private final List<Vec3d> connectedAnchors = new ArrayList<>();

    private double velocity = 0.0;
    private double ropeLength = 0.0;

    public LiftSystemManager(LiftPlatformEntity lift, LiftCounterWeightEntity counterweight) {
        this.liftEntity = lift;
        this.counterWeightEntity = counterweight;
        activeSystems.put(lift.getUuid(), this);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static LiftSystemManager getOrCreate(LiftPlatformEntity lift, LiftCounterWeightEntity counterweight) {
        return activeSystems.computeIfAbsent(lift.getUuid(), k -> new LiftSystemManager(lift, counterweight));
    }

    public void updateMovement() {
        if (this.liftEntity == null || this.counterWeightEntity == null) return;

        double liftMass = liftEntity.getMass();
        double counterWeightMass = counterWeightEntity.getMass();

        double acceleration = (counterWeightMass - liftMass) * 0.1; // Adjust factor as needed
        this.velocity += acceleration;

        this.liftEntity.setVelocity(new Vec3d(0, velocity, 0));
        this.liftEntity.move(MovementType.SELF, new Vec3d(0, velocity, 0));

        this.counterWeightEntity.setVelocity(new Vec3d(0, -this.velocity, 0));
        this.counterWeightEntity.move(MovementType.SELF, new Vec3d(0, -this.velocity, 0));

        this.velocity *= 0.98;
    }

    public void stopMovement() {
        this.velocity = 0;
    }
}
