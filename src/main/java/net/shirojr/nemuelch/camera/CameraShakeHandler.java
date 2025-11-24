package net.shirojr.nemuelch.camera;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Use...
 * <ul>
 *     <li>{@link #startNewDisplacement(int, int, Displacement, Displacement, Easing)}  startNewDisplacement}</li>
 *     <li>{@link #startFromCurrentDisplacement(int, int, Displacement, Easing)}  startFromCurrentDisplacement}</li>
 *     <li>{@link #stopAndResetDisplacement() stopAndResetDisplacement}</li>
 * </ul>
 * ...to control the Camera Shake {@link Displacement}
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CameraShakeHandler {
    private Easing easing;
    private Entity focusedEntity;
    @Nullable
    private Displacement startDisplacement;
    @Nullable
    private Displacement endDisplacement;

    private int duration;
    private int finalHoldDuration;
    private int elapsed;

    public CameraShakeHandler() {
        this.easing = Easing.LINEAR;
        this.focusedEntity = null;
    }

    // region getter & setter
    public Entity getFocusedEntity() {
        return focusedEntity;
    }

    public CameraShakeHandler setFocusedEntity(Entity focusedEntity) {
        this.focusedEntity = focusedEntity;
        return this;
    }

    public Displacement getInterpolatedDisplacement(float tickDelta) {
        if (!isActive() || getStartDisplacement() == null || getEndDisplacement() == null) {
            return Displacement.DEFAULT;
        }

        float progress = (getElapsed() + tickDelta) / (float) getDuration();
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);

        return getEasing().interpolate(progress, getStartDisplacement(), getEndDisplacement());
    }

    public @Nullable Displacement getStartDisplacement() {
        return startDisplacement;
    }

    public void setStartDisplacement(@Nullable Displacement startDisplacement) {
        this.startDisplacement = startDisplacement;
    }

    public @Nullable Displacement getEndDisplacement() {
        return endDisplacement;
    }

    public void setEndDisplacement(@Nullable Displacement endDisplacement) {
        this.endDisplacement = endDisplacement;
    }

    public Easing getEasing() {
        return easing;
    }

    public CameraShakeHandler setEasing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    public int getFinalHoldDuration() {
        return finalHoldDuration;
    }

    public void setFinalHoldDuration(int finalHoldDuration) {
        this.finalHoldDuration = Math.max(0, finalHoldDuration);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isActive() {
        if (getDuration() == 0 && getFinalHoldDuration() == 0) return false;
        return getElapsed() <= (getDuration() + getFinalHoldDuration());
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public void tick() {
        if (!isActive()) return;
        setElapsed(getElapsed() + 1);
    }
    //endregion

    public void startFromCurrentDisplacement(int duration, int finalHoldDuration, Displacement target, @Nullable Easing easing) {
        setDuration(duration);
        setFinalHoldDuration(finalHoldDuration);
        setElapsed(0);
        setStartDisplacement(getInterpolatedDisplacement(0));
        setEndDisplacement(target);
        if (easing != null) {
            setEasing(easing);
        }
    }

    public void startNewDisplacement(int duration, int finalHoldDuration, Displacement start, Displacement end, @Nullable Easing easing) {
        setDuration(duration);
        setFinalHoldDuration(finalHoldDuration);
        setElapsed(0);
        setStartDisplacement(start);
        setEndDisplacement(end);
        if (easing != null) {
            setEasing(easing);
        }
    }

    public void stopAndResetDisplacement() {
        setDuration(0);
        setFinalHoldDuration(0);
        setElapsed(0);
    }
}
