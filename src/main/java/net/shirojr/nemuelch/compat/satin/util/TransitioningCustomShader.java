package net.shirojr.nemuelch.compat.satin.util;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TransitioningCustomShader {
    public static final float THRESHOLD = 0.001f;

    private final Identifier identifier;
    private final List<Runnable> onStartCollector;
    private final List<Runnable> onFinishCollector;
    @Nullable
    private final ManagedShaderEffect managedShader;

    private final ShaderChannel persistentChannel;
    private final HashMap<Identifier, ShaderChannel> externalChannels;
    private boolean wasRendered;

    protected TransitioningCustomShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        this.identifier = identifier;
        this.onStartCollector = new ArrayList<>();
        this.onStartCollector.add(onStart);
        this.onFinishCollector = new ArrayList<>();
        this.onFinishCollector.add(onFinish);

        this.persistentChannel = new ShaderChannel();
        this.externalChannels = new HashMap<>();
        this.wasRendered = false;

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
        return persistentChannel.getCurrentState();
    }

    public float getTargetState() {
        return persistentChannel.getTargetState();
    }

    public float getTickDelta() {
        return persistentChannel.getTickDelta();
    }
    //endregion

    public float getEffectiveState() {
        float effective = this.persistentChannel.getCurrentState();
        for (Map.Entry<Identifier, ShaderChannel> entry : this.externalChannels.entrySet()) {
            effective = Math.max(effective, entry.getValue().getCurrentState());
        }
        return effective;
    }

    public boolean isRendered() {
        return getEffectiveState() > THRESHOLD;
    }

    public void render() {
        IllegalStateException e = new IllegalStateException("Tried to render custom Shader without it having specified how to be rendered");
        NeMuelch.LOGGER.error("Custom Shader had no rendering implementation yet", e);
        throw e;
    }

    public void updateStates(float tickDelta) {
        this.persistentChannel.update(tickDelta);
        this.externalChannels.values().forEach(channel -> channel.update(tickDelta));
        this.externalChannels.values().removeIf(channel -> channel.isTransitionInactive() && !channel.isRendered());
        this.refreshRenderedState();
    }

    public void startTransition(float targetState, int duration) {
        startTransition(getCurrentState(), targetState, duration);
    }

    public void startTransition(float startState, float targetState, int duration) {
        if (this.persistentChannel.getTargetState() == targetState) return;
        this.persistentChannel.startTransition(startState, targetState, duration);
    }

    public void setInstant(float normalizedFade) {
        this.persistentChannel.setTargetState(normalizedFade);
        this.persistentChannel.finish();
        this.refreshRenderedState();
    }

    public void setInstantExternalState(Identifier channelId, float value) {
        float clamped = MathHelper.clamp(value, 0, 1f);
        if (clamped <= THRESHOLD) {
            this.externalChannels.remove(channelId);
        } else {
            ShaderChannel channel = this.externalChannels.computeIfAbsent(channelId, id -> new ShaderChannel());
            channel.setTargetState(clamped);
            channel.finish();
        }
        this.refreshRenderedState();
    }

    public void clearExternalState(Identifier channelId) {
        this.externalChannels.remove(channelId);
        this.refreshRenderedState();
    }

    public void finish() {
        this.persistentChannel.finish();
        this.refreshRenderedState();
    }

    public void clear() {
        this.persistentChannel.clear();
        this.externalChannels.clear();
        this.refreshRenderedState();
        this.runOnFinish();
    }

    private void refreshRenderedState() {
        if (!NeMuelch.isIrisModLoaded()) return;
        boolean nowRendered = this.isRendered();
        if (nowRendered && !this.wasRendered) this.runOnStart();
        else if (!nowRendered && this.wasRendered) this.runOnFinish();
        this.wasRendered = nowRendered;
    }
}
