package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

public class StatusEffectHelper {
    private StatusEffectHelper() {
    }

    public static boolean isIn(StatusEffect effect, TagKey<StatusEffect> tag) {
        return Registries.STATUS_EFFECT.getEntry(effect).isIn(tag);
    }

}
