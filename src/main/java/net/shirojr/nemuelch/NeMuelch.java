package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shirojr.nemuelch.item.NeMuelchItems;

public class NeMuelch implements ModInitializer {
	public static final String MOD_ID = "nemuelch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NeMuelchItems.registerModItems();
	}

}

