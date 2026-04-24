package net.shirojr.nemuelch.init;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum NeMuelchToolMaterials implements ToolMaterial {
    GLOVE_LEATHER(79, 1, 0, 0, 15, Ingredient.empty()),
    CHAINED_MACE_COMPOSITE(1200, 12.0f, 5f, MiningLevels.DIAMOND, 15, Ingredient.empty());


    private final int durability, miningLevel, enchantability;
    private final float miningSpeedMultiplier, attackDamage;
    private final Supplier<Ingredient> repairIngredientSupplier;


    NeMuelchToolMaterials(int durability, float miningSpeedMultiplier, float attackDamage, int miningLevel, int enchantability,
                          Ingredient repairIngredientSupplier) {
        this.durability = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamage = attackDamage;
        this.miningLevel = miningLevel;
        this.enchantability = enchantability;
        this.repairIngredientSupplier = Suppliers.memoize(() -> repairIngredientSupplier);
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredientSupplier.get();
    }
}
