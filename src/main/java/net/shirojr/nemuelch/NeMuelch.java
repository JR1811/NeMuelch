package net.shirojr.nemuelch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.event.NeMuelchEvents;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.network.NeMuelchC2SPacketHandler;
import net.shirojr.nemuelch.painting.NeMuelchPaintings;
import net.shirojr.nemuelch.recipe.NeMuelchRecipes;
import net.shirojr.nemuelch.screen.handler.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.registry.NeMuelchRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class NeMuelch implements ModInitializer {

    public static final String MOD_ID = "nemuelch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier ENTITY_SPAWN_PACKET_ID = new Identifier(NeMuelch.MOD_ID, "spawn_packet");
    public static final Identifier PLAY_PARTICLE_PACKET_ID = new Identifier(NeMuelch.MOD_ID, "particle_packet");
    public static final Identifier EJECT_ROPER_ROPES_PACKET_ID = new Identifier(NeMuelch.MOD_ID, "eject_ropes_packet");
    public static final Identifier SOUND_PACKET_ID = new Identifier(NeMuelch.MOD_ID, "sound_packet");

    @Override
    public void onInitialize() {
        NeMuelchItems.registerModItems();
        NeMuelchBlocks.registerModBlocks();
        NeMuelchBlockEntities.registerBlockEntities();
        NeMuelchScreenHandlers.registerAllScreenHandlers();
        NeMuelchRecipes.registerRecipes();
        NeMuelchSounds.initializeSounds();
        NeMuelchPaintings.registerPaintings();
        NeMuelchRegistries.register();
        NeMuelchEffects.init();
        NeMuelchC2SPacketHandler.registerServerReceivers();
        NeMuelchEntities.register();
        NeMuelchEvents.registerEvents();

        GeckoLib.initialize();
        ConfigInit.init();

        if (FabricLoader.getInstance().isModLoaded("rpgz")) {
            NeMuelch.devLogger("RPGZ is installed. Dragging body feature may be compromised. A custom version is available at https://github.com/JR1811/RpgZ");
        }
    }

    public static void devLogger(String input) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        LOGGER.info("Dev - [ " + input + " ]");
    }
}

