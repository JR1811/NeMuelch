package net.shirojr.nemuelch.magic.reward;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.magic.Reward;

public class ItemDurabilityReward implements Reward {
    private int availableDurability;

    public ItemDurabilityReward(int distributableDurability) {
        this.availableDurability = distributableDurability;
    }

    @Override
    public void apply(LivingEntity entity) {
        for (ItemStack stack : entity.getItemsHand()) {
            if (!stack.isDamageable()) continue;
            this.processStack(stack);
        }
        if (entity instanceof PlayerEntity player) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isDamageable()) continue;
                if (this.processStack(stack)) return;
            }
        }
        for (ItemStack stack : entity.getArmorItems()) {
            if (!stack.isDamageable()) continue;
            if (this.processStack(stack)) return;
        }
    }

    /**
     * @return true, if some durability is still left over
     */
    private boolean processStack(ItemStack stack) {
        int tempDurability = stack.getDamage() + this.availableDurability;
        int newDurability = Math.min(tempDurability, stack.getMaxDamage());
        this.availableDurability -= newDurability;
        stack.setDamage(newDurability);
        return this.availableDurability > 0;
    }
}
