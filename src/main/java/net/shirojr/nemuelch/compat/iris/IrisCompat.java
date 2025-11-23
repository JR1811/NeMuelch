package net.shirojr.nemuelch.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisApiConfig;

public class IrisCompat {
    private static final IrisApiConfig config = IrisApi.getInstance().getConfig();

    private static boolean enabledShadersBuffer;

    public static void disableShaders() {
        enabledShadersBuffer = config.areShadersEnabled();
        if (!config.areShadersEnabled()) return;
        config.setShadersEnabledAndApply(false);
    }

    public static void resetOriginalShaderState() {
        if (enabledShadersBuffer) {
            config.setShadersEnabledAndApply(true);
        }
    }
}
