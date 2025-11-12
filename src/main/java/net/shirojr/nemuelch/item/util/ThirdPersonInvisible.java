package net.shirojr.nemuelch.item.util;

import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.init.NeMuelchTags;

public interface ThirdPersonInvisible {
    static boolean isInvisible(ItemStack stack) {
        if (stack.getItem() instanceof ThirdPersonInvisible) return true;
        return stack.isIn(NeMuelchTags.Items.BLOCK_THIRD_PERSON_RENDERING);
    }
}
