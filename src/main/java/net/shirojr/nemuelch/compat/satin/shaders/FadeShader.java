package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class FadeShader extends TransitioningCustomShader {
    public static final Identifier ZONE_FADE_CHANNEL = NeMuelch.getId("zone_fade");

    public FadeShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
    }

    public void setInstantZoneState(float zoneFade) {
        this.setInstantExternalState(ZONE_FADE_CHANNEL, zoneFade);
    }

    public void fadeToBlack(int duration) {
        this.startTransition(1.0f, duration);
    }

    public void fadeFromBlack(int duration) {
        this.startTransition(0f, duration);

    }

    @Override
    public void render() {
        if (getManagedShader() == null || !isRendered()) return;
        getManagedShader().findUniform1f("FadeAmount").set(getEffectiveState());
        Vec3d pos = getClient().gameRenderer.getCamera().getPos();
        getManagedShader().findUniform3f("CameraPos").set(pos.toVector3f());
        getManagedShader().render(getTickDelta());
    }
}
