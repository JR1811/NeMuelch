package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.statement.StatementCompat;
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
        NeMuelchFlammableRegistry.initialize();
        NeMuelchItemGroups.initialize();
        NeMuelchBlockEntities.initialize();
        NeMuelchScreenHandlers.initialize();
        NeMuelchRecipes.initialize();
        NeMuelchSounds.initialize();
        NeMuelchPaintings.initialize();
        NeMuelchStatusEffects.initialize();
        NeMuelchPotions.initialize();
        NeMuelchC2SNetworking.initialize();
        NeMuelchEntities.initialize();
        NeMuelchEvents.initializeCommon();
        NeMuelchTrackedData.initialize();
        NeMuelchConfigInit.initialize();
        NemuelchGameRules.initialize();
        NeMuelchDatapacks.initialize();
        NeMuelchArgumentTypes.initialize();
        NeMuelchEnchantments.initialize();
        NeMuelchWorldGen.initialize();
        NeMuelchTags.initialize();
        NeMuelchParticleTypes.initialize();
        NeMuelchEntityAttributes.initialize();
        NeMuelchDamageTypes.initialize();
        NeMuelchFuels.initialize();
        NeMuelchBiomes.initialize();
        NeMuelchDimensions.initialize();

        NeMuelchCustomRegistries.initialize();
        NeMuelchOccasions.initialize();

        StatementCompat.initialize();

        LOGGER.info("Who wants some milk?");
    }

    public static Identifier getId(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static boolean isSodiumModLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    public static boolean isSatinModLoaded() {
        return FabricLoader.getInstance().isModLoaded("satin");
    }

    public static boolean isDehydrationModLoaded() {
        return FabricLoader.getInstance().isModLoaded("dehydration");
    }

    public static boolean isIrisModLoaded() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }
}
