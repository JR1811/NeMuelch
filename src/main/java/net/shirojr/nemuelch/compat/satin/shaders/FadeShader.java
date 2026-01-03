package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class FadeShader extends TransitioningCustomShader {
    public FadeShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
    }

    public void fadeToBlack(int duration) {
        if (getTargetState() == 1.0f) return;
        setStartState(getCurrentState());
        setTargetState(1.0f);
        setFrame(0);
        setDuration(duration);
        NeMuelch.LOGGER.info("started fading to black [duration: {}]", duration);
    }

    public void fadeFromBlack(int duration) {
        if (getTargetState() == 0.0f) return;
        setStartState(getCurrentState());
        setTargetState(0);
        setFrame(0);
        setDuration(duration);
        NeMuelch.LOGGER.info("started fading from black back to normal [duration: {}]", duration);
    }

    @Override
    public void render() {
        if (getManagedShader() == null || !isRendered()) return;
        getManagedShader().findUniform1f("FadeAmount").set(getCurrentState());
        Vec3d pos = getClient().gameRenderer.getCamera().getPos();
        getManagedShader().findUniform3f("CameraPos").set(pos.toVector3f());
        getManagedShader().render(getTickDelta());
    }
}
