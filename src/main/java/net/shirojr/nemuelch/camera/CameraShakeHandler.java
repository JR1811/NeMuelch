package net.shirojr.nemuelch.camera;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CameraShakeHandler {
    private final DisplacementSequence displacementSequence;

    private Entity focusedEntity;

    public CameraShakeHandler() {
        this.focusedEntity = null;
        this.displacementSequence = new DisplacementSequence(List.of());
    }

    public DisplacementSequence getDisplacementSequence() {
        return displacementSequence;
    }

    public Entity getFocusedEntity() {
        return focusedEntity;
    }

    public CameraShakeHandler setFocusedEntity(Entity focusedEntity) {
        this.focusedEntity = focusedEntity;
        return this;
    }

    public void addDisplacement(Displacement displacement, int activeDuration, int holdDuration, Easing easing) {
        this.displacementSequence.addEntry(displacement, activeDuration, holdDuration, easing);
    }

    public void tick() {
        if (!this.displacementSequence.isActive()) return;
        this.displacementSequence.tick();
    }

    public void startFreshDisplacement(int duration, int finalHoldDuration, Displacement target, @Nullable Easing easing) {
        this.displacementSequence.clear();
        this.displacementSequence.addEntry(target, duration, finalHoldDuration, easing == null ? Easing.LINEAR : easing);
    }

    public void stopDisplacement() {
        this.displacementSequence.clear();
    }
}
