package net.shirojr.nemuelch.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchRecipes;
import net.shirojr.nemuelch.item.util.InnerOuterColoring;
import net.shirojr.nemuelch.util.helper.ColorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class InnerOuterColoringRecipe extends SpecialCraftingRecipe {
    public InnerOuterColoringRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int width = inventory.getWidth();
        int baseIndex = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof InnerOuterColoring) {
                if (baseIndex != -1) return false;
                baseIndex = i;
            }
        }
        if (baseIndex == -1) return false;

        int baseRow = baseIndex / width;
        List<DyeItem> outerDyes = new ArrayList<>();
        List<DyeItem> innerDyes = new ArrayList<>();

        for (int i = 0; i < inventory.size(); i++) {
            if (i == baseIndex) continue;
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof DyeItem dye)) return false;

            int row = i / width;

            if (row < baseRow) outerDyes.add(dye);
            else if (row > baseRow) innerDyes.add(dye);
            else return false;
        }
        return !outerDyes.isEmpty() || !innerDyes.isEmpty();
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        int width = inventory.getWidth();
        int baseIndex = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof InnerOuterColoring) {
                if (baseIndex != -1) return ItemStack.EMPTY;
                baseIndex = i;
            }
        }
        if (baseIndex == -1) return ItemStack.EMPTY;

        int baseRow = baseIndex / width;
        List<DyeItem> outerDyes = new ArrayList<>();
        List<DyeItem> innerDyes = new ArrayList<>();

        for (int i = 0; i < inventory.size(); i++) {
            if (i == baseIndex) continue;
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof DyeItem dye)) return ItemStack.EMPTY;

            int row = i / width;

            if (row < baseRow) outerDyes.add(dye);
            else if (row > baseRow) innerDyes.add(dye);
            else return ItemStack.EMPTY;
        }
        if (outerDyes.isEmpty() && innerDyes.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = inventory.getStack(baseIndex).copyWithCount(1);
        if (!outerDyes.isEmpty()) {
            OptionalInt existing = InnerOuterColoring.getOuterColor(result);
            int blended = existing.isPresent() ? ColorHelper.getBlendColor(outerDyes, existing.getAsInt()) : ColorHelper.getBlendColor(outerDyes);
            InnerOuterColoring.setOuterColor(result, blended);
        }
        if (!innerDyes.isEmpty()) {
            OptionalInt existing = InnerOuterColoring.getInnerColor(result);
            int blended = existing.isPresent() ? ColorHelper.getBlendColor(innerDyes, existing.getAsInt()) : ColorHelper.getBlendColor(innerDyes);
            InnerOuterColoring.setInnerColor(result, blended);
        }
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NeMuelchRecipes.INNER_OUTER_COLORING_SERIALIZER;
    }
}
