package net.shirojr.nemuelch.compat.satin;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import net.shirojr.nemuelch.compat.satin.shaders.FadeShader;
import net.shirojr.nemuelch.compat.satin.util.ShaderHolder;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;

import java.util.function.Function;

public class NeMuelchShaderManager {
    private static int activeShaders = 0;

    public static final FadeShader FADE = register("shaders/post/fade.json", identifier ->
            FadeShader.getInstance(
                    identifier,
                    NeMuelchShaderManager::incrementActiveShaders,
                    NeMuelchShaderManager::decrementActiveShaders
            )
    );


    private static <T extends ShaderHolder> T register(String path, Function<Identifier, T> entry) {
        if (!NeMuelch.isSatinPresent()) {
            throw new RuntimeException("Tried to register [ %s ] Shader without Satin API".formatted(path));
        }
        Identifier identifier = NeMuelch.getId(path);
        return entry.apply(identifier);
    }

    @SuppressWarnings("unused")
    public static int getActiveShadersCount() {
        return activeShaders;
    }

    /**
     * Needs to be called from {@link ShaderHolder} implementing Shader class to help with Iris Settings locking
     */
    public static void incrementActiveShaders() {
        int prevCount = activeShaders;
        activeShaders += 1;
        if (prevCount == 0 && NeMuelchClient.isIrisModLoaded()) {
            IrisCompat.disableShaders();
            IrisCompat.setShaderToggleLock(true);
        }
    }

    /**
     * Needs to be called from {@link ShaderHolder} implementing Shader class to help with Iris Settings locking
     */
    public static void decrementActiveShaders() {
        int prevCount = activeShaders;
        activeShaders = Math.max(0, activeShaders - 1);
        if (prevCount > 0 && activeShaders == 0) {
            if (NeMuelchConfigInit.CONFIG.restoreIrisShaderRenderingOnFinishedInternalShader) {
                IrisCompat.resetOriginalShaderState();
                IrisCompat.setShaderToggleLock(false);
            }
        }
    }

    public static void initialize() {
        // static initialisation
    }
}
