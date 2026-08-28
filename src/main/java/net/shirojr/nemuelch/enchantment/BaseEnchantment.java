package net.shirojr.nemuelch.enchantment;

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

import java.util.Collection;

public class BaseEnchantment extends Enchantment {
    private final boolean isCurse;
    private final int maxLevel;
    private final Object2BooleanFunction<Enchantment> isIncompatible;

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse, int maxLevel, Object2BooleanFunction<Enchantment> isIncompatible) {
        super(weight, target, slotTypes);
        this.isCurse = isCurse;
        this.maxLevel = maxLevel;
        this.isIncompatible = isIncompatible;
    }

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse, int maxLevel) {
        this(weight, target, slotTypes, isCurse, maxLevel, key -> false);
    }

    public BaseEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes, boolean isCurse) {
        this(weight, target, slotTypes, isCurse, 1, key -> false);
    }

    @Override
    public boolean isCursed() {
        return this.isCurse;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return !this.isIncompatible.getBoolean(other);
    }

    public static Object2BooleanFunction<Enchantment> getIncompatibilities(Collection<Enchantment> enchantments) {
        //noinspection SuspiciousMethodCalls
        return enchantments::contains;
    }
}
