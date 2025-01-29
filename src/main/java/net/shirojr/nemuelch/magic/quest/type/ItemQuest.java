package net.shirojr.nemuelch.magic.quest.type;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.magic.Quest;
import net.shirojr.nemuelch.magic.Reward;

public abstract class ItemQuest extends Quest {
    private final Item requestedItem;
    private final int removalAmount;

    public ItemQuest(String name, Reward reward, Item requestedItem, int removalAmount) {
        super(name, reward);
        this.requestedItem = requestedItem;
        this.removalAmount = removalAmount;
    }

    public ItemQuest(String name, Reward reward, Item requestedItem) {
        super(name, reward);
        this.requestedItem = requestedItem;
        this.removalAmount = 0;
    }

    public void checkInventory(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (analyseAndProcess(stack)) return;
        }
        for (ItemStack stack : entity.getItemsEquipped()) {
            if (analyseAndProcess(stack)) return;
        }
        if (entity instanceof PlayerEntity player) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                if (analyseAndProcess(player.getInventory().getStack(i))) return;
            }
        }
    }

    private boolean analyseAndProcess(ItemStack stack) {
        if (stack.getItem().equals(this.requestedItem)) {
            if (this.removalAmount > 0) {
                stack.decrement(Math.min(stack.getCount(), this.removalAmount));
            }
            markCompleted();
            return true;
        }
        return false;
    }
}
