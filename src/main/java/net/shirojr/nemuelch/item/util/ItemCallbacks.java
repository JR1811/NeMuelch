package net.shirojr.nemuelch.item.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ItemCallbacks {
    default <T extends LivingEntity> void nemuelch$onBroken(T user, ItemStack stack) {
    }

    @SuppressWarnings("unused")
    default void nemuelch$onDecremented(ItemStack stack, int amount) {
    }
}
