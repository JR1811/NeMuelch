package net.shirojr.nemuelch.init;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum NeMuelchArmorMaterials implements ArmorMaterial {
    BARREL_MATERIAL("barrel_wood", 16, new int[]{2, 5, 7, 2}, 28,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, Ingredient.ofItems(Items.IRON_INGOT)),
    ROYAL_GUARD_ARMOR("royal_guard_armor", 150, new int[]{3, 6, 8, 3}, 10,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2.0f, 0.3f, Ingredient.ofItems(NeMuelchItems.PRESTINURAN_INGOT)),
    FALLEN_GUARD_ARMOR("fallen_guard_armor", 150, new int[]{4, 7, 9, 4}, 10,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2.0f, 0.4f, Ingredient.ofItems(NeMuelchItems.VERZITRAN_INGOT));


    private static final int[] BASE_DURABILITY;
    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredientSupplier;

    NeMuelchArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantability,
                           SoundEvent equipSound, float toughness, float knockbackResistance, Ingredient repairIngredientSupplier) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredientSupplier = Suppliers.memoize(() -> repairIngredientSupplier);
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getEntitySlotId()] * this.durabilityMultiplier;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return this.protectionAmounts[slot.getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredientSupplier.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    static {
        BASE_DURABILITY = new int[]{13, 15, 16, 11};
    }
}
