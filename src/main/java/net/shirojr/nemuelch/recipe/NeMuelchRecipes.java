package net.shirojr.nemuelch.recipe;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchRecipes {

    public static void registerRecipes() {

        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Serializer.ID),
                PestcaneStationRecipe.Serializer.INSTANCE);

        Registry.register(Registry.RECIPE_TYPE, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Type.ID),
                PestcaneStationRecipe.Type.INSTANCE);
    }
}
