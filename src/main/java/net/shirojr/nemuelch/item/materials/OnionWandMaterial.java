package net.shirojr.nemuelch.item.materials;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.shirojr.nemuelch.init.ConfigInit;

public class OnionWandMaterial implements ToolMaterial {

    private final Supplier<Ingredient> repairIngredient = Suppliers.memoize(Ingredient::empty);

    public static final OnionWandMaterial INSTANCE = new OnionWandMaterial();


    @Override
    public int getDurability() {
        return ConfigInit.CONFIG.onion.getToolData().getDurability();
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
