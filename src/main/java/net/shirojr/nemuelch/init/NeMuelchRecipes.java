package net.shirojr.nemuelch.init;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.recipe.BlightHandMixingRecipe;
import net.shirojr.nemuelch.recipe.BookWrapperRecipe;
import net.shirojr.nemuelch.recipe.FillSmokingPipeRecipe;
import net.shirojr.nemuelch.recipe.PestcaneStationRecipe;

@SuppressWarnings("unused")
public interface NeMuelchRecipes {

    RecipeSerializer<PestcaneStationRecipe> PESTCANE_STATION_SERIALIZER =
            registerSerializer(PestcaneStationRecipe.Serializer.ID, PestcaneStationRecipe.Serializer.INSTANCE);
    RecipeType<PestcaneStationRecipe> PESTCANE_STATION_TYPE =
            registerType(PestcaneStationRecipe.Type.ID, PestcaneStationRecipe.Type.INSTANCE);

    RecipeSerializer<BlightHandMixingRecipe> POISON_HAND_MIXING_SERIALIZER =
            registerSerializer("blight_hand_mixing", BlightHandMixingRecipe.Serializer.INSTANCE);
    RecipeType<BlightHandMixingRecipe> POISON_HAND_MIXING_TYPE =
            registerType("blight_hand_mixing", BlightHandMixingRecipe.Type.INSTANCE);

    RecipeSerializer<FillSmokingPipeRecipe> FILL_SMOKING_PIPE_SERIALIZER =
            registerSerializer("fill_smoking_pipe", FillSmokingPipeRecipe.Serializer.INSTANCE);
    RecipeType<FillSmokingPipeRecipe> FILL_SMOKING_PIPE_TYPE =
            registerType("fill_smoking_pipe", FillSmokingPipeRecipe.Type.INSTANCE);

    RecipeSerializer<BookWrapperRecipe> BOOK_WRAPPER_SERIALIZER =
            registerSerializer("book_wrapper", BookWrapperRecipe.Serializer.INSTANCE);


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
