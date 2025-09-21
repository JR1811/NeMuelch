package net.shirojr.nemuelch.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.HandInventory;

public abstract class AbstractHandCraftingRecipe implements Recipe<HandInventory> {
    private final Identifier id;

    public AbstractHandCraftingRecipe(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public abstract boolean matches(HandInventory inventory, World world);

    @Override
    public abstract ItemStack craft(HandInventory inventory, DynamicRegistryManager registryManager);

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public abstract ItemStack getOutput(DynamicRegistryManager registryManager);

    @Override
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    public abstract RecipeType<?> getType();
}
