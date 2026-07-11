package net.shirojr.nemuelch.enchantment;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MultiTargetBaseEnchantment extends BaseEnchantment {
    private final List<EnchantmentTarget> enchantmentTargets;

    public MultiTargetBaseEnchantment(Rarity weight, List<EnchantmentTarget> enchantmentTargets, EquipmentSlot[] slotTypes, boolean isCurse, int  maxLevel) {
        super(weight, enchantmentTargets.get(0), slotTypes, isCurse, maxLevel);
        this.enchantmentTargets = enchantmentTargets;
    }

    public List<EnchantmentTarget> getEnchantmentTargets() {
        return enchantmentTargets;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        for (EnchantmentTarget entry : this.enchantmentTargets) {
            if (entry.isAcceptableItem(stack.getItem())) {
                return true;
            }
        }
        return false;
    }
}
