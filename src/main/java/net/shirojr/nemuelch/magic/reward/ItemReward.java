package net.shirojr.nemuelch.magic.reward;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.shirojr.nemuelch.magic.Reward;

import java.util.ArrayList;
import java.util.List;

public class ItemReward implements Reward {
    private final List<ItemStack> rewardStacks = new ArrayList<>();

    public ItemReward(List<ItemStack> rewardStacks) {
        this.rewardStacks.addAll(rewardStacks);
    }

    @Override
    public void apply(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            this.rewardStacks.forEach(stack -> player.getInventory().offerOrDrop(stack));
        } else {
            DefaultedList<ItemStack> droppedStacks = DefaultedList.ofSize(this.rewardStacks.size());
            droppedStacks.addAll(this.rewardStacks.stream().map(ItemStack::copy).toList());
            ItemScatterer.spawn(entity.world, entity.getBlockPos(), droppedStacks);
        }
    }
}
