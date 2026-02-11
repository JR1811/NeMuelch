package net.shirojr.nemuelch.compat.satin;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import net.shirojr.nemuelch.compat.satin.shaders.CrimsonPhaseShader;
import net.shirojr.nemuelch.compat.satin.shaders.FadeShader;
import net.shirojr.nemuelch.compat.satin.util.LazyShaderHolder;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NeMuelchShaderManager {
    private static int activeShaders = 0;

    public static final List<LazyShaderHolder<? extends TransitioningCustomShader>> ALL_SHADERS = new ArrayList<>();

    public static final LazyShaderHolder<FadeShader> FADE = register("shaders/post/fade.json", identifier ->
            new FadeShader(
                    identifier,
                    NeMuelchShaderManager::incrementActiveShaders,
                    NeMuelchShaderManager::decrementActiveShaders
            )
    );

    public static final LazyShaderHolder<CrimsonPhaseShader> CRIMSON_PHASE = register("shaders/post/crimson_phase.json", identifier ->
            new CrimsonPhaseShader(
                    identifier,
                    NeMuelchShaderManager::incrementActiveShaders,
                    NeMuelchShaderManager::decrementActiveShaders
            )
    );


    private static <T extends TransitioningCustomShader> LazyShaderHolder<T> register(String path, Function<Identifier, T> entry) {
        Identifier identifier = NeMuelch.getId(path);
        LazyShaderHolder<T> lazy = new LazyShaderHolder<>(() -> entry.apply(identifier));
        ALL_SHADERS.add(lazy);
        return lazy;
    }

    public static int getOrdinal(LazyShaderHolder<? extends TransitioningCustomShader> shader) {
        return ALL_SHADERS.indexOf(shader);
    }

    public static LazyShaderHolder<? extends TransitioningCustomShader> fromOrdinal(int ordinal) {
        return ALL_SHADERS.get(ordinal);
    }

    @SuppressWarnings("unused")
    public static int getActiveShadersCount() {
        return activeShaders;
    }

    public static void setActiveShadersCount(int activeShaders) {
        NeMuelchShaderManager.activeShaders = activeShaders;
    }

    public static void incrementActiveShaders() {
        int prevCount = getActiveShadersCount();
        setActiveShadersCount(getActiveShadersCount() + 1);
        if (prevCount == 0 && NeMuelch.isIrisModLoaded()) {
            IrisCompat.disableShaders();
            IrisCompat.setShaderToggleLock(true);
        }
    }

    public static void decrementActiveShaders() {
        int prevCount = getActiveShadersCount();
        setActiveShadersCount(Math.max(0, getActiveShadersCount() - 1));
        if (prevCount > 0 && getActiveShadersCount() == 0) {
            IrisCompat.setShaderToggleLock(false);
            if (NeMuelchConfigInit.CONFIG.restoreIrisShaderRenderingOnFinishedInternalShader) {
                IrisCompat.resetOriginalShaderState();
            }
        }
    }

    public static void initialize() {
        // static initialisation
    }
}
