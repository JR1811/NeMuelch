package net.shirojr.nemuelch.compat.satin.shaders;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import net.shirojr.nemuelch.compat.satin.util.ShaderHolder;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;

@SuppressWarnings("unused")
public class FadeShaderManager implements ShaderHolder {
    public static final float THRESHOLD = 0.001f;

    private static FadeShaderManager instance = null;
    private final Identifier identifier;
    private final ManagedShaderEffect fadeShader;

    private static float currentFade = 0.0f;
    private static float startFade = 0.0f;
    private static float targetFade = 0.0f;
    private static int duration = 0;
    private static int frame = 0;
    private static float tickDelta;


    private FadeShaderManager(Identifier identifier) {
        this.identifier = identifier;
        NeMuelch.LOGGER.info("Creating FadeShaderManager with identifier: {}", identifier);

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

    public static FadeShaderManager getInstance(Identifier identifier) {
        if (instance == null) {
            instance = new FadeShaderManager(identifier);
        }
        return instance;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    public void fadeToBlack(int duration) {
        if (targetFade == 1.0f) return;
        startFade = currentFade;
        targetFade = 1.0f;
        frame = 0;
        FadeShaderManager.duration = duration;
        NeMuelch.LOGGER.info("started fading to black [duration: {}]", duration);
    }

    public void fadeFromBlack(int duration) {
        if (targetFade == 0.0f) return;
        startFade = currentFade;
        targetFade = 0;
        frame = 0;
        FadeShaderManager.duration = duration;
        NeMuelch.LOGGER.info("started fading from black back to normal [duration: {}]", duration);
    }

    public void setFadeInstant(float normalizedFade) {
        targetFade = MathHelper.clamp(normalizedFade, 0, 1);
        finish();
    }

    public void setStaticFadeAmount(float fade) {
        targetFade = fade;
        currentFade = fade;
        frame = 0;
        duration = 0;
    }

    public static float getCurrentFade() {
        return currentFade;
    }

    @Override
    public boolean isRendered() {
        return currentFade > THRESHOLD;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFadeTransitionActive() {
        return Math.abs(currentFade - targetFade) > THRESHOLD;
    }

    public boolean isIncreasingFade() {
        if (!isFadeTransitionActive()) return false;
        return currentFade < targetFade;
    }

    public boolean isDecreasingFade() {
        if (!isFadeTransitionActive()) return false;
        return currentFade > targetFade;
    }

    @Override
    public void render() {
        if (fadeShader == null || !isRendered()) return;
        this.fadeShader.findUniform1f("FadeAmount").set(currentFade);
        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        this.fadeShader.findUniform3f("CameraPos").set(pos.toVector3f());
        this.fadeShader.render(tickDelta);
    }

    @Override
    public ManagedShaderEffect getShader() {
        return fadeShader;
    }

    @Override
    public void update(float tickDelta) {
        ShaderHolder.super.update(tickDelta);
        if (!isFadeTransitionActive() || duration == 0) {
            if (frame != 0) {
                finish();
            }
            return;
        }
        FadeShaderManager.tickDelta = tickDelta;
        NeMuelch.LOGGER.info("Updated Fade Shader - Current Fade: {}", currentFade);
        frame++;

        if (Math.abs(currentFade - targetFade) <= THRESHOLD) {
            finish();
            return;
        }

        float progress = MathHelper.clamp((float) frame / (float) duration, 0.0f, 1.0f);
        currentFade = MathHelper.lerp(progress, startFade, targetFade);

        if (frame >= duration) {
            finish();
        }
    }

    @Override
    public void finish() {
        currentFade = targetFade;
        frame = 0;
        duration = 0;
        tickDelta = 0;
        if (NeMuelchClient.isIrisModLoaded()) {
            if (isRendered()) {
                IrisCompat.disableShaders();
            } else if (NeMuelchConfigInit.CONFIG.restoreIrisShaderRenderingOnFinishedInternalShader) {
                IrisCompat.resetOriginalShaderState();
            }
        }
    }

    public static void clearFade() {
        currentFade = 0;
        startFade = 0;
        targetFade = 0;
        frame = 0;
        duration = 0;
        tickDelta = 0;
    }
}
