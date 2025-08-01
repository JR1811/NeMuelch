package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.network.NeMuelchC2SNetworking;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.slf4j.Logger;

public class NeMuelch implements ModInitializer {
    public static final String MOD_ID = "nemuelch";
    public static final Logger LOGGER = LoggerUtil.LOGGER;


    @Override
    public void onInitialize() {
        NeMuelchItems.initialize();
        NeMuelchBlocks.initialize();
        NeMuelchItemGroups.initialize();
        NeMuelchBlockEntities.initialize();
        NeMuelchScreenHandlers.initialize();
        NeMuelchRecipes.initialize();
        NeMuelchSounds.initialize();
        NeMuelchPaintings.initialize();
        NeMuelchEffects.initialize();
        NeMuelchC2SNetworking.initialize();
        NeMuelchEntities.initialize();
        NeMuelchEvents.initializeCommon();
        NeMuelchTrackedData.initialize();
        NeMuelchConfigInit.initialize();

        LOGGER.info("Who wants some milk?");
    }

    public static Identifier getId(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
