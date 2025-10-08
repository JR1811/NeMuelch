package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.util.constants.NbtKeys;

public class NeMuelchColorProviders {
    static {
        ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::bookWrapperColoring, NeMuelchItems.BOOK_WRAPPER);
    }

    private static int bookWrapperColoring(ItemStack stack, int index) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0;

        if (index == 0 && nbt.contains(NbtKeys.WRAPPER)) {
            return nbt.getInt(NbtKeys.WRAPPER);
        }
        if (index == 1 && nbt.contains(NbtKeys.STRIP)) {
            return nbt.getInt(NbtKeys.STRIP);
        }
        if (index == 2 && nbt.contains(NbtKeys.SIGIL)) {
            return nbt.getInt(NbtKeys.SIGIL);
        }

        return 0;
    }

    public static void initialize() {
        // static initialisation
    }
}
