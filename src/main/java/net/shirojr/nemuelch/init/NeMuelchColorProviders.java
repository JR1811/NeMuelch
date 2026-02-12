package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.item.custom.supportItem.BookWrapperItem;

public class NeMuelchColorProviders {
    static {
        ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::bookWrapperColoring, NeMuelchItems.BOOK_WRAPPER);
        for (MiasmaItem miasmaItem : NeMuelchItems.MIASMA_ITEMS) {
            ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::miasmaColoring, miasmaItem);
        }
    }

    private static int bookWrapperColoring(ItemStack stack, int index) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0;
        Integer wrapperColor = BookWrapperItem.Part.WRAPPER.getColor(stack);
        Integer stripColor = BookWrapperItem.Part.STRIP.getColor(stack);
        Integer sigilColor = BookWrapperItem.Part.SIGIL.getColor(stack);

        if (index == 0 && wrapperColor != null) {
            return wrapperColor;
        }
        if (index == 1 && stripColor != null) {
            return stripColor;
        }
        if (index == 2 && sigilColor != null) {
            return sigilColor;
        }
        return 0;
    }

    public static int miasmaColoring(ItemStack stack, int index) {
        return MiasmaItem.getColor(stack, MiasmaItem.Part.values()[index]);
    }

    public static void initialize() {
        // static initialisation
    }
}
