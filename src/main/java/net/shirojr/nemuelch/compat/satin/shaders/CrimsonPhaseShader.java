package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class CrimsonPhaseShader extends TransitioningCustomShader {
    private int frame;

    public CrimsonPhaseShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
        this.modifyOnStart(runnables -> runnables.add(() -> this.frame = 0));
    }

    @Override
    public void updateStates(float tickDelta) {
        super.updateStates(tickDelta);
        if (isRendered()) this.frame++;
    }

    @Override
    public void render() {
        if (getManagedShader() == null || !isRendered()) return;
        getManagedShader().findUniform1f("Intensity").set(getEffectiveState());
        getManagedShader().findUniform1f("Time").set(this.frame + getTickDelta());

        getManagedShader().render(getTickDelta());
    }
}
