package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.PestCaneRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class NeMuelch implements ModInitializer {
	public static final String MOD_ID = "nemuelch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier FUSE = new Identifier("extraalchemy","fuse");	//TODO: Identifier for fuse effect (extraalchemy mod)

	@Override
	public void onInitialize() {
		NeMuelchItems.registerModItems();

		GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestCaneRenderer());
	}

}

