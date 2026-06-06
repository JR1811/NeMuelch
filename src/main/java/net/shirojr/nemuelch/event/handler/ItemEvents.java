package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchRecipes;
import net.shirojr.nemuelch.recipe.AbstractHandCraftingRecipe;
import net.shirojr.nemuelch.util.HandInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemEvents implements UseItemCallback {
    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        ItemStack poisonHandMixingResult = attemptHandMixingRecipes(NeMuelchRecipes.POISON_HAND_MIXING_TYPE, player);
        if (poisonHandMixingResult != null) {
            return TypedActionResult.success(poisonHandMixingResult);
        }
        ItemStack fillSmokingPipeResult = attemptHandMixingRecipes(NeMuelchRecipes.FILL_SMOKING_PIPE_TYPE, player);
        if (fillSmokingPipeResult != null) {
            return TypedActionResult.success(fillSmokingPipeResult);
        }
        return TypedActionResult.pass(stackInHand);
    }

    @Nullable
    private <T extends AbstractHandCraftingRecipe> ItemStack attemptHandMixingRecipes(RecipeType<T> recipeType, PlayerEntity player) {
        HandInventory inventory = new HandInventory(player);
        World world = player.getWorld();
        Optional<T> firstMatch = world.getRecipeManager().getFirstMatch(recipeType, inventory, world);
        if (firstMatch.isEmpty()) return null;
        AbstractHandCraftingRecipe recipe = firstMatch.get();
        ItemStack result = recipe.craft(inventory, world.getRegistryManager());
        if (result.isEmpty()) return null;
        if (world instanceof ServerWorld serverWorld) {
            DefaultedList<ItemStack> remainder = recipe.getRemainder(inventory);
            for (ItemStack remainedStack : remainder) {
                player.getInventory().offerOrDrop(remainedStack);
            }
            inventory.decrement();
            player.getInventory().offerOrDrop(result);
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM,
                    SoundCategory.PLAYERS, 2f, 1f);
        }
        return result;
    }
}
