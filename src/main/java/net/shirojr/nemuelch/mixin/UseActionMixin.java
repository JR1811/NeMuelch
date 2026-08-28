package net.shirojr.nemuelch.mixin;

import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UseAction.class)
public enum UseActionMixin {
    NEMUELCH_CLIMBING
}
