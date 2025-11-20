package net.shirojr.nemuelch.camera;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CameraShakeHandler {
    @Nullable
    private Easing easing;
    private Entity focusedEntity;
    private Displacement displacement;
    private float tickDelta;

    public CameraShakeHandler() {
        this.tickDelta = 0;
        this.easing = null;
        this.focusedEntity = null;
        this.displacement = new Displacement();
    }

    // region getter & setter

    public Entity getFocusedEntity() {
        return focusedEntity;
    }

    public CameraShakeHandler setFocusedEntity(Entity focusedEntity) {
        this.focusedEntity = focusedEntity;
        return this;
    }

    public Displacement getDisplacement() {
        return displacement;
    }

    public CameraShakeHandler setDisplacement(Displacement displacement) {
        this.displacement = displacement;
        return this;
    }

    public @Nullable Easing getEasing() {
        return easing;
    }

    public CameraShakeHandler setEasing(@Nullable Easing easing) {
        this.easing = easing;
        return this;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public CameraShakeHandler setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
        return this;
    }
    //endregion

    public void stop() {
        setEasing(null);
        setTickDelta(0);
    }

    public void update() {

    }
}
