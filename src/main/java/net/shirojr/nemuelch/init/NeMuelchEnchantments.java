package net.shirojr.nemuelch.init;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.enchantment.BaseEnchantment;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchEnchantments {
    List<Enchantment> ALL_ENCHANTMENTS = new ArrayList<>();
    EquipmentSlot[] ALL_ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    Enchantment CURSE_OF_THE_BARE = register("curse_of_the_bare", new BaseEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.ARMOR, ALL_ARMOR_SLOTS, true));

    @SuppressWarnings("SameParameterValue")
    private static <T extends Enchantment> T register(String name, T entry) {
        T registeredEntry = Registry.register(Registries.ENCHANTMENT, NeMuelch.getId(name), entry);
        ALL_ENCHANTMENTS.add(registeredEntry);
        return registeredEntry;
    }

    static void initialize() {
        // static initialisation
    }
}
