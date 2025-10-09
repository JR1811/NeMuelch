package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchRecipes;
import net.shirojr.nemuelch.recipe.BlightHandMixingRecipe;
import net.shirojr.nemuelch.util.HandInventory;

import java.util.Optional;

public class ItemEvents implements UseItemCallback {
    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        HandInventory inventory = new HandInventory(player);
        Optional<BlightHandMixingRecipe> recipeFinder = world.getRecipeManager().getFirstMatch(NeMuelchRecipes.POISON_HAND_MIXING_TYPE, inventory, world);
        if (recipeFinder.isEmpty()) {
            return TypedActionResult.pass(stackInHand);
        }
        ItemStack result = recipeFinder.get().craft(inventory, world.getRegistryManager());
        if (world instanceof ServerWorld serverWorld) {

            DefaultedList<ItemStack> remainder = recipeFinder.get().getRemainder(inventory);
            for (ItemStack remainedStack : remainder) {
                player.getInventory().offerOrDrop(remainedStack);
            }
            inventory.decrement();
            player.getInventory().offerOrDrop(result);
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.PLAYERS, 1f, 1f);
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_HONEY_BLOCK_SLIDE, SoundCategory.PLAYERS, 1f, 1f);
        }
        return TypedActionResult.success(result);
    }
}
