package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.entity.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.recipe.NeMuelchRecipes;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class NeMuelch implements ModInitializer {

	public static final String MOD_ID = "nemuelch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityType<ArkaduscaneProjectileEntity> ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(NeMuelch.MOD_ID, "arkaduscane_projectile"),
			FabricEntityTypeBuilder.<ArkaduscaneProjectileEntity>create(SpawnGroup.MISC, ArkaduscaneProjectileEntity::new)
					.dimensions(EntityDimensions.fixed(0.3F, 0.3F))   // projectile size
					.trackRangeBlocks(4).trackedUpdateRate(10)
					.build()
	);

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

