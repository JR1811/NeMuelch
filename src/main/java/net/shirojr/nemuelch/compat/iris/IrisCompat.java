package net.shirojr.nemuelch.compat.iris;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisApiConfig;
import net.minecraft.client.MinecraftClient;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.duck.IrisConfigShaderToggleLock;

public class IrisCompat {
    private static final IrisApiConfig config = IrisApi.getInstance().getConfig();

    private static boolean enabledShadersCache = false;

    public static void disableShaders() {
        if (!config.areShadersEnabled()) return;
        try {
            Iris.toggleShaders(MinecraftClient.getInstance(), false);
        } catch (Exception e) {
            NeMuelch.LOGGER.error("Iris Shader was not disabled for internal Shader", e);
        }

        enabledShadersCache = config.areShadersEnabled();
        /*if (!config.areShadersEnabled()) return;
        config.setShadersEnabledAndApply(false);*/
    }

    public static void resetOriginalShaderState() {
        if (enabledShadersCache) {
            //config.setShadersEnabledAndApply(true);

            try {
                Iris.toggleShaders(MinecraftClient.getInstance(), true);
            } catch (Exception e) {
                NeMuelch.LOGGER.error("Iris Shader was not enabled after internal Shader finished", e);
            }
        }
    }

    public static IrisConfigShaderToggleLock getShaderToggleLocker() {
        return (IrisConfigShaderToggleLock) Iris.getIrisConfig();
    }

    public static void setShaderToggleLock(boolean locked) {
        getShaderToggleLocker().neMuelch$setLocked(locked);
    }

    public static void onInteractWithLocked() {
        getShaderToggleLocker().onLockedInteraction(MinecraftClient.getInstance());
    }
}
