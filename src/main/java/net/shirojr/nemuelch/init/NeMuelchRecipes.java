package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.recipe.PestcaneStationRecipe;

public class NeMuelchRecipes {
    static {
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Serializer.ID),
                PestcaneStationRecipe.Serializer.INSTANCE);

        Registry.register(Registries.RECIPE_TYPE, new Identifier(NeMuelch.MOD_ID, PestcaneStationRecipe.Type.ID),
                PestcaneStationRecipe.Type.INSTANCE);
    }

    public static void initialize() {
        // static initialisation
    }
}
