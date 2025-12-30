package net.shirojr.nemuelch.compat.satin.shaders;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.compat.satin.util.ShaderHolder;

@SuppressWarnings("unused")
public class FadeShader implements ShaderHolder {
    public static final float THRESHOLD = 0.001f;

    private static FadeShader instance = null;
    private final Identifier identifier;
    private final Runnable onStart;
    private final Runnable onFinish;
    private final ManagedShaderEffect fadeShader;

    private float currentFade = 0.0f;
    private float startFade = 0.0f;
    private float targetFade = 0.0f;
    private int duration = 0;
    private int frame = 0;
    private float tickDelta;


    private FadeShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        this.identifier = identifier;
        this.onStart = onStart;
        this.onFinish = onFinish;
        NeMuelch.LOGGER.info("Creating FadeShader with identifier: {}", identifier);

        try {
            this.fadeShader = ShaderEffectManager.getInstance().manage(identifier);
            NeMuelch.LOGGER.info("Successfully created ManagedShaderEffect: {}", fadeShader != null);
            if (fadeShader != null) {
                NeMuelch.LOGGER.info("Shader effect class: {}", fadeShader.getClass().getName());
            }
        } catch (Exception e) {
            NeMuelch.LOGGER.error("Failed to create shader effect", e);
            throw e;
        }
    }

    public static FadeShader getInstance(Identifier identifier, Runnable onStart, Runnable onFinish) {
        if (instance == null) {
            instance = new FadeShader(identifier, onStart, onFinish);
        }
        return instance;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public Runnable onStarted() {
        return this.onStart;
    }

    @Override
    public Runnable onFinished() {
        return this.onFinish;
    }

    public float getCurrentFade() {
        return currentFade;
    }

    public void setCurrentFade(float fade) {
        this.currentFade = fade;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public void fadeToBlack(int duration) {
        if (targetFade == 1.0f) return;
        startFade = getCurrentFade();
        targetFade = 1.0f;
        frame = 0;
        this.duration = duration;
        NeMuelch.LOGGER.info("started fading to black [duration: {}]", duration);
    }

    public void fadeFromBlack(int duration) {
        if (targetFade == 0.0f) return;
        startFade = getCurrentFade();
        targetFade = 0;
        frame = 0;
        this.duration = duration;
        NeMuelch.LOGGER.info("started fading from black back to normal [duration: {}]", duration);
    }

    public void setFadeInstant(float normalizedFade) {
        targetFade = MathHelper.clamp(normalizedFade, 0, 1);
        finish();
    }

    public void setStaticFadeAmount(float fade) {
        targetFade = fade;
        setCurrentFade(fade);
        frame = 0;
        duration = 0;
    }

    @Override
    public boolean isRendered() {
        return getCurrentFade() > THRESHOLD;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFadeTransitionActive() {
        return Math.abs(getCurrentFade() - targetFade) > THRESHOLD;
    }

    public boolean isIncreasingFade() {
        if (!isFadeTransitionActive()) return false;
        return getCurrentFade() < targetFade;
    }

    public boolean isDecreasingFade() {
        if (!isFadeTransitionActive()) return false;
        return getCurrentFade() > targetFade;
    }

    @Override
    public void render() {
        if (fadeShader == null || !isRendered()) return;
        this.fadeShader.findUniform1f("FadeAmount").set(getCurrentFade());
        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        this.fadeShader.findUniform3f("CameraPos").set(pos.toVector3f());
        this.fadeShader.render(getTickDelta());
    }

    @Override
    public ManagedShaderEffect getShader() {
        return fadeShader;
    }

    @Override
    public void update(float tickDelta) {
        ShaderHolder.super.update(tickDelta);
        if (!isFadeTransitionActive() || getDuration() == 0) {
            if (frame != 0) {
                finish();
            }
            return;
        }
        setTickDelta(tickDelta);
        NeMuelch.LOGGER.info("Updated Fade Shader - Current Fade: {}", currentFade);
        frame++;

        if (Math.abs(getCurrentFade() - targetFade) <= THRESHOLD) {
            finish();
            return;
        }

        float progress = MathHelper.clamp((float) frame / (float) getDuration(), 0.0f, 1.0f);
        setCurrentFade(MathHelper.lerp(progress, startFade, targetFade));

        if (frame >= getDuration()) {
            finish();
        }
    }

    @Override
    public void finish() {
        setCurrentFade(targetFade);
        frame = 0;
        setDuration(0);
        setTickDelta(0);
        if (NeMuelchClient.isIrisModLoaded()) {
            if (isRendered()) {
                onStarted().run();
            } else {
                onFinished().run();
            }
        }
    }

    public void clearFade() {
        setCurrentFade(0);
        startFade = 0;
        targetFade = 0;
        frame = 0;
        setDuration(0);
        setTickDelta(0);
    }
}
