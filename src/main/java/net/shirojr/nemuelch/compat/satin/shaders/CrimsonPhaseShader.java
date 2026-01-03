package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

//TODO: - edge detection on depth sampler instead?
public class CrimsonPhaseShader extends TransitioningCustomShader {
    private float nearClamp, farClamp;

    public CrimsonPhaseShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
        this.nearClamp = 0.05f;
        this.farClamp = 256f;
    }

    public float getNearClamp() {
        return nearClamp;
    }

    public void setNearClamp(float nearClamp) {
        this.nearClamp = nearClamp;
    }

    public float getFarClamp() {
        return farClamp;
    }

    public void setFarClamp(float farClamp) {
        this.farClamp = farClamp;
    }

    @Override
    public void render() {
        if (getManagedShader() == null || !isRendered()) return;
        getManagedShader().findUniform1f("Intensity").set(getCurrentState());
        getManagedShader().findUniform1f("Time").set(getDuration() + getTickDelta());
        getManagedShader().setUniformValue("Near", getNearClamp());
        getManagedShader().setUniformValue("Far", getFarClamp());

        getManagedShader().render(getTickDelta());
    }
}
