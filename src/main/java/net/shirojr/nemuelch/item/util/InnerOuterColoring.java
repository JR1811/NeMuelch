package net.shirojr.nemuelch.item.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public interface InnerOuterColoring {
    static OptionalInt getInnerColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY)) return OptionalInt.empty();
        return OptionalInt.of(nbt.getInt(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY));
    }

    static void setInnerColor(ItemStack stack, int color) {
        stack.getOrCreateNbt().putInt(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY, color);
    }

    static OptionalInt getOuterColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY)) return OptionalInt.empty();
        return OptionalInt.of(nbt.getInt(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY));
    }

    static void setOuterColor(ItemStack stack, int color) {
        stack.getOrCreateNbt().putInt(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY, color);
    }

    static boolean matchingColors(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null || b == null) return false;
        if (!(a.getItem() instanceof InnerOuterColoring) || !(b.getItem() instanceof InnerOuterColoring)) return false;
        return getInnerColor(a).equals(getInnerColor(b)) && getOuterColor(a).equals(getOuterColor(b));
    }
}
