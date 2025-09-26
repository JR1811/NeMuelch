package net.shirojr.nemuelch.init;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.recipe.PestcaneStationRecipe;
import net.shirojr.nemuelch.recipe.BlightHandMixingRecipe;

@SuppressWarnings("unused")
public interface NeMuelchRecipes {

    RecipeSerializer<PestcaneStationRecipe> PESTCANE_STATION_RECIPE_SERIALIZER =
            registerSerializer(PestcaneStationRecipe.Serializer.ID, PestcaneStationRecipe.Serializer.INSTANCE);

    RecipeSerializer<BlightHandMixingRecipe> POISON_HAND_MIXING_RECIPE_SERIALIZER =
            registerSerializer("blight_hand_mixing", BlightHandMixingRecipe.Serializer.INSTANCE);


    RecipeType<PestcaneStationRecipe> PESTCANE_STATION_RECIPE_TYPE =
            registerType(PestcaneStationRecipe.Type.ID, PestcaneStationRecipe.Type.INSTANCE);

    RecipeType<BlightHandMixingRecipe> POISON_HAND_MIXING_RECIPE_TYPE =
            registerType("blight_hand_mixing", BlightHandMixingRecipe.Type.INSTANCE);


    private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, NeMuelch.getId(name), serializer);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerType(String name, RecipeType<T> type) {
        return Registry.register(Registries.RECIPE_TYPE, NeMuelch.getId(name), type);
    }

    static void initialize() {
        // static initialisation
    }
}
