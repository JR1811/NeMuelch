package net.shirojr.nemuelch.item.materials;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class NeMuelchMaterials implements ToolMaterial {

    private final Supplier<Ingredient> repairIngredient = Suppliers.memoize(Ingredient::empty);

    public static final NeMuelchMaterials INSTANCE = new NeMuelchMaterials();


    @Override
    public int getDurability() {
        return 79;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

}
