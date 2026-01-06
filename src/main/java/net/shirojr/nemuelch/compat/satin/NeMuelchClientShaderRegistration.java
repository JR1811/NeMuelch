package net.shirojr.nemuelch.compat.satin;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

import java.util.List;
import java.util.function.Function;

public class NeMuelchClientShaderRegistration {
    private static <T extends TransitioningCustomShader> T register(String path, Function<Identifier, T> entry, List<TransitioningCustomShader> allShaders) {
        if (!NeMuelch.isSatinModLoaded()) {
            throw new RuntimeException("Tried to register [ %s ] Shader without Satin API".formatted(path));
        }
        Identifier identifier = NeMuelch.getId(path);
        T registeredEntry = entry.apply(identifier);
        allShaders.add(registeredEntry);
        return registeredEntry;
    }
}
