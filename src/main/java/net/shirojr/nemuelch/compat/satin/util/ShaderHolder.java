package net.shirojr.nemuelch.compat.satin.util;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import net.minecraft.util.Identifier;

public interface ShaderHolder {
    Identifier getIdentifier();

    ManagedShaderEffect getShader();

    void render();

    boolean isRendered();

    void finish();

    default void update(float tickDelta) {
        // leave empty if not used
    }
}
