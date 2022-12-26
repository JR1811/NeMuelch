package net.shirojr.nemuelch;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.config.NeMuelchConfig;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.entity.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.painting.NeMuelchPaintings;
import net.shirojr.nemuelch.recipe.NeMuelchRecipes;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
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
        NeMuelchSounds.initializeSounds();
        NeMuelchPaintings.registerPaintings();
        NeMuelchRegistries.register();
        NeMuelchEffects.registerEffects();

        GeckoLib.initialize();
        ConfigInit.init();
    }

    // * to test if mods are loaded *
    // boolean installedDehydration = FabricLoader.getInstance().isModLoaded("dehydration")
    // boolean installedRevive = FabricLoader.getInstance().isModLoaded("revive")
}

