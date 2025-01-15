package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.network.NeMuelchC2SNetworking;
import net.shirojr.nemuelch.init.NeMuelchTrackedData;
import net.shirojr.nemuelch.util.LoggerUtil;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

public class NeMuelch implements ModInitializer {
    public static final String MOD_ID = "nemuelch";
    public static final Logger LOGGER = LoggerUtil.LOGGER;


    @Override
    public void onInitialize() {
        NeMuelchItems.initialize();
        NeMuelchBlocks.initialize();
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

        GeckoLib.initialize();

        LOGGER.info("Who wants some milk?");
    }
}
