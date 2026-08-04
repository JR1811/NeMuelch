package net.shirojr.nemuelch.recipe;

import net.minecraft.item.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.shirojr.nemuelch.inventory.HandInventory;
import net.shirojr.nemuelch.item.custom.supportItem.OintmentItem;

import java.util.List;
import java.util.function.Predicate;

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
    public DefaultedList<ItemStack> getRemainder(HandInventory inventory) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        List<Predicate<Item>> glassBottles = List.of(
                item -> item instanceof PotionItem,
                item -> item instanceof SplashPotionItem,
                item -> item instanceof LingeringPotionItem
        );
        List<Predicate<Item>> bowls = List.of(
                item -> item instanceof SuspiciousStewItem,
                item -> item instanceof OintmentItem
        );
        List<Predicate<Item>> arrows = List.of(
                item -> item instanceof TippedArrowItem
        );
        for (int i = 0; i < inventory.getStacks().size(); i++) {
            ItemStack itemStack = inventory.getStacks().get(i);
            boolean foundRemainder = false;
            if (!itemStack.getRecipeRemainder().isEmpty()) {
                remainder.set(i, itemStack.getRecipeRemainder());
                foundRemainder = true;
            }
            if (foundRemainder) continue;
            for (Predicate<Item> glassBottle : glassBottles) {
                if (glassBottle.test(itemStack.getItem())) {
                    remainder.set(i, new ItemStack(Items.GLASS_BOTTLE));
                    foundRemainder = true;
                    break;
                }
            }
            if (foundRemainder) continue;
            for (Predicate<Item> bowl : bowls) {
                if (bowl.test(itemStack.getItem())) {
                    remainder.set(i, new ItemStack(Items.BOWL));
                    foundRemainder = true;
                    break;
                }
            }
            if (foundRemainder) continue;
            for (Predicate<Item> arrow : arrows) {
                if (arrow.test(itemStack.getItem())) {
                    remainder.set(i, new ItemStack(Items.ARROW));
                    break;
                }
            }
        }
        return remainder;
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    public abstract RecipeType<?> getType();
}
