package net.shirojr.nemuelch.mixin.compat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.text.NumberFormat;

@SuppressWarnings({"unused", "MissingUnique"})
@Mixin(ItemStack.class)
public class StaccFixItemStackMixinCrash {
    @Environment(EnvType.SERVER)
    private static NumberFormat FORMAT;
}