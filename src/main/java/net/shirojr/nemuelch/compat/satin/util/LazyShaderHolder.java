package net.shirojr.nemuelch.compat.satin.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

public class LazyShaderHolder<T extends TransitioningCustomShader> {
    private final Supplier<T> supplier;
    private T instance;

    public LazyShaderHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getInstance() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            throw new IllegalStateException("Shader code executed on SERVER environment");
        }
        if (this.instance == null) this.instance = this.supplier.get();
        return this.instance;
    }
}
