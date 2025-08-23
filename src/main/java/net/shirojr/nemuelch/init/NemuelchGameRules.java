package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public interface NemuelchGameRules {
    GameRules.Key<GameRules.BooleanRule> CUSTOM_RESPAWN_LOCATIONS = GameRuleRegistry.register("respawnLocations",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<GameRules.BooleanRule> RESPAWN_LOCATIONS_CONFIG_FALLBACK = GameRuleRegistry.register("respawnLocationsDefaultFromConfig",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    GameRules.Key<GameRules.BooleanRule> RESPAWN_LOCATIONS_EXCLUDE_PREVIOUS = GameRuleRegistry.register("respawnLocationsExcludePrevious",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    static void initialize() {
        // static initialisation
    }
}
