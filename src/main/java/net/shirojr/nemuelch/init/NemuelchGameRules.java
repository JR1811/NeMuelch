package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;

public interface NemuelchGameRules {
    GameRules.Key<GameRules.BooleanRule> CUSTOM_RESPAWN_LOCATIONS = GameRuleRegistry.register("respawnLocations",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<GameRules.BooleanRule> RESPAWN_LOCATIONS_CONFIG_FALLBACK = GameRuleRegistry.register("respawnLocationsDefaultFromConfig",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    GameRules.Key<GameRules.BooleanRule> RESPAWN_LOCATIONS_EXCLUDE_PREVIOUS = GameRuleRegistry.register("respawnLocationsExcludePrevious",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    GameRules.Key<GameRules.BooleanRule> BLIGHT_SPREADING = GameRuleRegistry.register("blightSpreading",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<GameRules.BooleanRule> BLIGHT_SPREADING_CHUNKS = GameRuleRegistry.register("blightSpreadingToNextChunks",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<GameRules.IntRule> BLIGHT_TICK_SPEED = GameRuleRegistry.register("blightSpreadingTickSpeed",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(BlightChunkComponent.DEFAULT_TICK_SPEED, 20));
    GameRules.Key<GameRules.BooleanRule> BLIGHT_OP_HINTS = GameRuleRegistry.register("blightAdminHints",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<GameRules.IntRule> BLIGHT_MAX_SPREAD_ATTEMPTS = GameRuleRegistry.register("blightMaxSpreadAttempts",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(BlightChunkComponent.DEFAULT_TICK_SPEED, 1));

    GameRules.Key<GameRules.BooleanRule> PRINT_CONNECTION_TEXTS = GameRuleRegistry.register("connectionTextPrinting",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

    GameRules.Key<GameRules.IntRule> MEAT_BLOCK_DIGESTION_DURATION = GameRuleRegistry.register("meatBlockDigestionDuration",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(3000, 1));

    GameRules.Key<GameRules.BooleanRule> THIRD_PERSON_ADMIN_ITEM_RENDERING_BLOCKING = GameRuleRegistry.register("thirdPersonAdminItemRendering",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false, (server, booleanRule) -> {
                for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeBoolean(booleanRule.get());
                    ServerPlayNetworking.send(target, NetworkIdentifiers.THIRD_PERSON_ITEM_RENDERING, buf);
                }
            })
    );

    GameRules.Key<DoubleRule> PULL_UP_VERT_STRENGTH = GameRuleRegistry.register("pullUpVerticalStrength",
            GameRules.Category.MISC, GameRuleFactory.createDoubleRule(0.05, (server, doubleRule) -> {
                for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeDouble(doubleRule.get());
                    ServerPlayNetworking.send(target, NetworkIdentifiers.PULL_UP_VERT_STRENGTH_GAMERULE_SYNC, buf);
                }
            }));

    GameRules.Key<GameRules.IntRule> EMPTY_BOAT_DESPAWN_DURATION = GameRuleRegistry.register("boatEmptyDespawnDuration",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(12000, -1));

    GameRules.Key<GameRules.IntRule> BOAT_DEEP_WATER_DEPTH = GameRuleRegistry.register("boatDeepWaterDepth",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(20, -1));

    GameRules.Key<GameRules.IntRule> BOAT_DEEP_WATER_CHECK_INTERVAL = GameRuleRegistry.register("boatDeepWaterCheckInterval",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(200, -1));

    GameRules.Key<GameRules.IntRule> BOAT_DEEP_WATER_ENDURANCE = GameRuleRegistry.register("boatDeepWaterEnduranceDuration",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(500, 0, (server, intRule) -> {
                        for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeVarInt(intRule.get());
                            ServerPlayNetworking.send(target, NetworkIdentifiers.DEEP_WATER_BOAT_ENDURANCE_SYNC, buf);
                        }
                    }
            )
    );

    GameRules.Key<GameRules.BooleanRule> CRATE_STORES_ENTITIES = GameRuleRegistry.register("canCrateStoreEntity",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

    GameRules.Key<GameRules.IntRule> PLAYER_LEFT_FLEETING_NOTE_DURATION = GameRuleRegistry.register("playerLeftNoteDuration",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(2400, 20));
    GameRules.Key<GameRules.BooleanRule> PLAYER_LEFT_FLEETING_NOTE_HIDE_NAME = GameRuleRegistry.register("playerLeftNoteHideName",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    GameRules.Key<GameRules.BooleanRule> PLAYER_LEFT_FLEETING_NOTES = GameRuleRegistry.register("playerLeftNotes",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {

            }));
    GameRules.Key<GameRules.IntRule> BLOCK_FINDER_INTERVAL = GameRuleRegistry.register("blockFinderInterval",
            GameRules.Category.MISC, GameRuleFactory.createIntRule(200, 40));

    static void initialize() {
        // static initialisation
    }
}
