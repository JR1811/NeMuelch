package net.shirojr.nemuelch.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class BaseEnchantment extends Enchantment {
    private final boolean isCurse;

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse) {
        super(weight, target, slotTypes);
        this.isCurse = isCurse;
    }

    @Override
    public boolean isCursed() {
        return this.isCurse;
    }
}
