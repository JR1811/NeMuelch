package net.shirojr.nemuelch.init;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.recipe.PestcaneStationRecipe;

public class NeMuelchRecipes {
    static {
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Serializer.ID),
                PestcaneStationRecipe.Serializer.INSTANCE);

        Registry.register(Registry.RECIPE_TYPE, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Type.ID),
                PestcaneStationRecipe.Type.INSTANCE);
    }

    public static void initialize() {
        // static initialisation
    }
}
