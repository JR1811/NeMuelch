package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

import java.util.HashSet;

@SuppressWarnings("unused")
public interface NeMuelchPotions {
    HashSet<Potion> ALL_POTIONS = new HashSet<>();

    Potion IMMINENT_HEALING = registerPotion("imminent_healing", NeMuelchStatusEffects.DEFERRED_HEALTH, 1800, 0,
            Potions.AWKWARD, Ingredient.ofItems(NeMuelchItems.LARD));
    Potion IMMINENT_STRONG_HEALING = registerPotion("imminent_strong_healing", NeMuelchStatusEffects.DEFERRED_HEALTH, 3600, 2,
            IMMINENT_HEALING, Ingredient.ofItems(Items.GLOWSTONE_DUST));
    Potion IMMINENT_QUICK_HEALING = registerPotion("imminent_quick_healing", NeMuelchStatusEffects.DEFERRED_HEALTH, 200, 0,
            IMMINENT_HEALING, Ingredient.ofItems(Items.REDSTONE));

    Potion IMMINENT_HARMING = registerPotion("imminent_harming", NeMuelchStatusEffects.DEFERRED_DAMAGE, 1800, 0,
            Potions.AWKWARD, Ingredient.ofItems(NeMuelchItems.ROTTEN_MEAT_LUMP));
    Potion IMMINENT_STRONG_HARMING = registerPotion("imminent_strong_harming", NeMuelchStatusEffects.DEFERRED_DAMAGE, 3600, 2,
            IMMINENT_HARMING, Ingredient.ofItems(Items.GLOWSTONE_DUST));
    Potion IMMINENT_LONG_HARMING = registerPotion("imminent_long_harming", NeMuelchStatusEffects.DEFERRED_DAMAGE, 7200, 1,
            IMMINENT_HARMING, Ingredient.ofItems(Items.REDSTONE));

    Potion SLIMING = registerPotion("sliming", NeMuelchStatusEffects.SLIMED, 100, 0,
            Potions.AWKWARD, Ingredient.ofItems(Items.SLIME_BALL));
    Potion LONG_SLIMING = registerPotion("long_sliming", NeMuelchStatusEffects.SLIMED, 2500, 0,
            SLIMING, Ingredient.ofItems(Items.REDSTONE));

    Potion ACID_BURN = registerPotion("acid_burning", NeMuelchStatusEffects.ACID_BURN, 250, 0,
            NeMuelchPotions.SLIMING, Ingredient.ofItems(NeMuelchItems.SOAP));
    Potion STRONG_ACID_BURNING = registerPotion("strong_acid_burning", NeMuelchStatusEffects.ACID_BURN, 100, 3,
            ACID_BURN, Ingredient.ofItems(Items.GLOWSTONE_DUST));
    Potion LONG_ACID_BURNING = registerPotion("long_acid_burning", NeMuelchStatusEffects.ACID_BURN, 2500, 1,
            ACID_BURN, Ingredient.ofItems(Items.REDSTONE));


    static Potion registerPotion(String name, StatusEffect effect, int duration, int amplifier,
                                 Potion input, Ingredient ingredient) {
        Potion potion = Registry.register(Registries.POTION, NeMuelch.getId(name), new Potion(new StatusEffectInstance(effect, duration, amplifier)));
        FabricBrewingRecipeRegistry.registerPotionRecipe(input, ingredient, potion);
        ALL_POTIONS.add(potion);
        return potion;
    }

    static void initialize() {
        // static initialisation
    }
}
