package net.shirojr.nemuelch.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class BaseEnchantment extends Enchantment {
    private final boolean isCurse;
    private final int maxLevel;

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse, int maxLevel) {
        super(weight, target, slotTypes);
        this.isCurse = isCurse;
        this.maxLevel = maxLevel;
    }

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse) {
        this(weight, target, slotTypes, isCurse, 1);
    }

    @Override
    public boolean isCursed() {
        return this.isCurse;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }
}
