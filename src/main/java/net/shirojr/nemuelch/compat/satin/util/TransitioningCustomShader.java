package net.shirojr.nemuelch.compat.satin.util;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransitioningCustomShader {
    public static final float THRESHOLD = 0.001f;

    private final Identifier identifier;
    private final List<Runnable> onStartCollector;
    private final List<Runnable> onFinishCollector;
    @Nullable
    private final ManagedShaderEffect managedShader;

    private float currentState = 0;
    private float startState = 0;
    private float targetState = 0;
    private int duration = 0;
    private int frame = 0;
    private float tickDelta = 0;

    protected TransitioningCustomShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        this.identifier = identifier;
        this.onStartCollector = new ArrayList<>();
        this.onStartCollector.add(onStart);
        this.onFinishCollector = new ArrayList<>();
        this.onFinishCollector.add(onFinish);

        LoggerUtil.devLogger("Creating %s shader".formatted(getIdentifier()));

        try {
            this.managedShader = ShaderEffectManager.getInstance().manage(getIdentifier());
            if (getManagedShader() != null) {
                LoggerUtil.devLogger("Created %s shader successfully".formatted(getIdentifier()));
            } else {
                throw new NullPointerException("No Shader found with Identifier: " + getIdentifier());
            }
        } catch (Exception e) {
            NeMuelch.LOGGER.error("Failed to create shader effect", e);
            throw e;
        }
    }

    //region Getter & Setter
    public Identifier getIdentifier() {
        return identifier;
    }

    public final void runOnStart() {
        this.onStartCollector.forEach(Runnable::run);
    }

    public final void modifyOnStart(Consumer<List<Runnable>> onStartList) {
        onStartList.accept(this.onStartCollector);
    }

    public final void runOnFinish() {
        this.onFinishCollector.forEach(Runnable::run);
    }

    public final void modifyOnFinish(Consumer<List<Runnable>> onFinishedList) {
        onFinishedList.accept(this.onFinishCollector);
    }

    @NotNull
    public MinecraftClient getClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            NullPointerException e = new NullPointerException("No valid client found for shader rendering");
            NeMuelch.LOGGER.error("How did you even do that???", e);
            throw e;
        }
        return client;
    }

    @Nullable
    public ManagedShaderEffect getManagedShader() {
        return managedShader;
    }

    public float getCurrentState() {
        return currentState;
    }

    public void setCurrentState(float currentState) {
        this.currentState = currentState;
    }

    public float getStartState() {
        return startState;
    }

    public void setStartState(float startState) {
        this.startState = startState;
    }

    public float getTargetState() {
        return targetState;
    }

    public void setTargetState(float targetState) {
        this.targetState = targetState;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }
    //endregion

    public boolean isRendered() {
        return getCurrentState() > THRESHOLD;
    }

    public float getProgress() {
        return MathHelper.clamp((float) getFrame() / (float) getDuration(), 0.0f, 1.0f);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTransitionActive() {
        return Math.abs(getCurrentState() - getTargetState()) > THRESHOLD;
    }

    public void render() {
        IllegalStateException e = new IllegalStateException("Tried to render custom Shader without it having specified how to be rendered");
        NeMuelch.LOGGER.error("Custom Shader had no rendering implementation yet", e);
        throw e;
    }

    public void updateStates(float tickDelta) {
        if (!isRendered() || getDuration() == 0) {
            if (getFrame() != 0) {
                finish();
            }
            return;
        }
        setTickDelta(tickDelta);
        setFrame(getFrame() + 1);
        if (!isTransitionActive()) {
            finish();
            return;
        }
        setCurrentState(MathHelper.lerp(getProgress(), getStartState(), getTargetState()));
        if (getFrame() >= getDuration()) {
            finish();
        }
    }

    public void setInstant(float normalizedFade) {
        setTargetState(MathHelper.clamp(normalizedFade, 0, 1));
        finish();
    }

    public void finish() {
        setCurrentState(getTargetState());
        setFrame(0);
        setDuration(0);
        setTickDelta(0);
        if (NeMuelchClient.isIrisModLoaded()) {
            if (isRendered()) {
                runOnStart();
            } else {
                runOnFinish();
            }
        }
    }

    public void clear() {
        setCurrentState(0);
        setStartState(0);
        setTargetState(0);
        setFrame(0);
        setDuration(0);
        setTickDelta(0);
    }
}
