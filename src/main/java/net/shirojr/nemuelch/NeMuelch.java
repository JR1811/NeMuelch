package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.recipe.NeMuelchRecipes;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class NeMuelch implements ModInitializer {

	public static final String MOD_ID = "nemuelch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NeMuelchItems.registerModItems();

		NeMuelchBlocks.registerModBlocks();
		NeMuelchBlockEntities.registerBlockEntities();

		NeMuelchScreenHandlers.registerAllScreenHandlers();
		NeMuelchRecipes.registerRecipes();

		GeckoLib.initialize();
	}
}

