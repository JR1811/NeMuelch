package net.shirojr.nemuelch.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class ItemStackUtil {
    public static void decrementInNonCreative(ItemStack stack, int amount, @NotNull PlayerEntity player) {
        if (player.isCreative()) return;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        stack.decrement(amount);
    }
}
