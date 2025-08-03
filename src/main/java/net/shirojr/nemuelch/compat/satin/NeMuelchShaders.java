package net.shirojr.nemuelch.compat.satin;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.shaders.FadeShaderManager;
import net.shirojr.nemuelch.compat.satin.util.ShaderHolder;

import java.util.function.Function;

public interface NeMuelchShaders {
    FadeShaderManager FADE = register("shaders/post/fade.json", FadeShaderManager::getInstance);

    private static <T extends ShaderHolder> T register(String path, Function<Identifier, T> entry) {
        if (!NeMuelch.isSatinPresent()) {
            throw new RuntimeException("Tried to register [ %s ] Shader without Satin API".formatted(path));
        }
        Identifier identifier = NeMuelch.getId(path);
        return entry.apply(identifier);
    }

    static void initialize() {
        // static initialisation
    }
}
