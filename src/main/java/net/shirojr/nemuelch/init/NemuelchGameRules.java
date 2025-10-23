package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;

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
    GameRules.Key<GameRules.BooleanRule> PRINT_CONNECTION_TEXTS = GameRuleRegistry.register("connectionTextPrinting",
            GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

    static void initialize() {
        // static initialisation
    }
}
