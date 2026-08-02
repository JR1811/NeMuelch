package net.shirojr.nemuelch.compat.satin.util;

import net.minecraft.util.math.MathHelper;

public class ShaderChannel {
    private float currentState;
    private float startState;
    private float targetState;

    private int duration;
    private int frame;
    private float tickDelta;

    public ShaderChannel(float currentState, float startState, float targetState, int duration) {
        this.currentState = currentState;
        this.startState = startState;
        this.targetState = targetState;
        this.duration = duration;
    }

    public ShaderChannel() {
        this(0f, 0f, 0f, 0);
    }

    public float getCurrentState() {
        return this.currentState;
    }

    public void setCurrentState(float currentState) {
        this.currentState = MathHelper.clamp(currentState, 0, 1);
    }

    public void setStartState(float startState) {
        this.startState = MathHelper.clamp(startState, 0, 1);
    }

    public void setTargetState(float targetState) {
        this.targetState = MathHelper.clamp(targetState, 0, 1);
    }

    public float getTargetState() {
        return this.targetState;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public float getProgress() {
        return MathHelper.clamp((float) this.frame / this.duration, 0.0f, 1.0f);
    }

    public boolean isRendered() {
        return this.getCurrentState() > TransitioningCustomShader.THRESHOLD;
    }

    public boolean isTransitionInactive() {
        return !(Math.abs(getCurrentState() - this.targetState) > TransitioningCustomShader.THRESHOLD);
    }

    public void startTransition(float from, float to, int duration) {
        this.setStartState(from);
        this.setTargetState(to);
        this.frame = 0;
        this.duration = duration;
    }

    public void update(float tickDelta) {
        if (isTransitionInactive() || duration == 0) {
            if (frame != 0) this.finish();
            return;
        }
        this.tickDelta = tickDelta;
        this.frame++;
        if (isTransitionInactive()) {
            this.finish();
            return;
        }
        setCurrentState(MathHelper.lerp(getProgress(), this.startState, this.targetState));
        if (this.frame >= this.duration) {
            this.finish();
        }
    }

    public void finish() {
        this.setCurrentState(this.targetState);
        this.frame = 0;
        this.duration = 0;
        this.tickDelta = 0;
    }

    public void clear() {
        this.currentState = 0;
        this.startState = 0;
        this.targetState = 0;
        this.duration = 0;
        this.frame = 0;
        this.tickDelta = 0;
    }
}
