package net.shirojr.nemuelch.effect.util;

import net.minecraft.entity.effect.StatusEffect;

public interface UnremovableStatusEffectHolder {
    boolean neMuelch$forceStatusEffectsClear();

    boolean neMuelch$forceStatusEffectRemoval(StatusEffect effect);
}
