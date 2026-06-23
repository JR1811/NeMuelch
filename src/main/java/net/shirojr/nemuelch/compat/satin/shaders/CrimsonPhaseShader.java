package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class CrimsonPhaseShader extends TransitioningCustomShader {
    public CrimsonPhaseShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
    }

    @Override
    public void render() {
        if (getManagedShader() == null || !isRendered()) return;
        getManagedShader().findUniform1f("Intensity").set(getCurrentState());
        getManagedShader().findUniform1f("Time").set(getFrame() + getTickDelta());

        getManagedShader().render(getTickDelta());
    }
}
