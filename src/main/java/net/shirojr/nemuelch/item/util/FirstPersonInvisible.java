package net.shirojr.nemuelch.item.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.init.NeMuelchTags;

public interface FirstPersonInvisible {
    default boolean isInFirstPersonInvisibleState(ItemStack stack) {
        return true;
    }

    static boolean isInvisible(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return false;
        if (stack.getItem() instanceof FirstPersonInvisible invisible && invisible.isInFirstPersonInvisibleState(stack)) {
            return true;
        }
        return stack.isIn(NeMuelchTags.Items.BLOCK_FIRST_PERSON_RENDERING);
    }
}
