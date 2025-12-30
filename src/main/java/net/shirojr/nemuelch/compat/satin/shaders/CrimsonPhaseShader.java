package net.shirojr.nemuelch.compat.satin.shaders;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class CrimsonPhaseShader extends TransitioningCustomShader {
    public CrimsonPhaseShader(Identifier identifier, Runnable onStart, Runnable onFinish) {
        super(identifier, onStart, onFinish);
    }
}
