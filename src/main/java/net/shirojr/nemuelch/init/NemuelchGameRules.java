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
            GameRules.Category.MISC, GameRuleFactory.createDoubleRule(3000, 1));


    static void initialize() {
        // static initialisation
    }
}
