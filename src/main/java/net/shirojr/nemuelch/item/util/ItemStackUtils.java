package net.shirojr.nemuelch.item.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class ItemStackUtils {
    public static void igniteTorch(ItemStack stack, BlockPos soundPos, PlayerEntity player, ServerWorld world) {
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        player.getInventory().offerOrDrop(new ItemStack(Items.TORCH));
        world.playSound(null, soundPos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                SoundCategory.BLOCKS, 1f, 1f);
    }
}
