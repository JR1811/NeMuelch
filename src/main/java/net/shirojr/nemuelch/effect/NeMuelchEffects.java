package net.shirojr.nemuelch.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.custom.*;

public class NeMuelchEffects {
    public static StatusEffect SHIELDING_SKIN;
    public static StatusEffect PLAYTHING_OF_THE_UNSEEN_DEITY;
    public static StatusEffect LEVITATING_ABSOLUTION;
    public static StatusEffect STUCK;
    public static StatusEffect WELL_RESTED;

    public static StatusEffect registerStatusEffect (String name, StatusEffect statusEffect) {
        return Registry.register(Registry.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }


    // new ShieldingSkinEffect(StatusEffectCategory.HARMFUL, 3124687)

    public static void registerEffects() {
        SHIELDING_SKIN = registerStatusEffect("shielding_skin", new ShieldingSkinEffect(StatusEffectCategory.BENEFICIAL, 3124687));
        PLAYTHING_OF_THE_UNSEEN_DEITY = registerStatusEffect("plaything_of_the_unseen_deity", new PlaythingOfTheUnseenDeityEffect(StatusEffectCategory.HARMFUL, 3124687));
        LEVITATING_ABSOLUTION = registerStatusEffect("levitating_absolution", new LevitatingAbsolutionEffect(StatusEffectCategory.NEUTRAL, 111111));
        STUCK = registerStatusEffect("stuck", new StuckEffect(StatusEffectCategory.HARMFUL, 0));
        WELL_RESTED = registerStatusEffect("well_rested", new WellRestedEffect(StatusEffectCategory.BENEFICIAL, 0));
    }
}
