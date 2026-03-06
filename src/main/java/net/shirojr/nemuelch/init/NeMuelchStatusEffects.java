package net.shirojr.nemuelch.init;

import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.custom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchStatusEffects {
    List<StatusEffect> STUCK_EFFECTS = new ArrayList<>();

    ShieldingSkinEffect SHIELDING_SKIN = register("shielding_skin", new ShieldingSkinEffect(StatusEffectCategory.BENEFICIAL, 3124687));
    PlaythingOfTheUnseenDeityEffect PLAYTHING_OF_THE_UNSEEN_DEITY = register("plaything_of_the_unseen_deity", new PlaythingOfTheUnseenDeityEffect(StatusEffectCategory.HARMFUL, 3124687));
    LevitatingAbsolutionEffect LEVITATING_ABSOLUTION = register("levitating_absolution", new LevitatingAbsolutionEffect(StatusEffectCategory.NEUTRAL, 111111));
    StuckEffect STUCK_DEFAULT = registerStuckEffects("stuck", new StuckEffect(StatusEffectCategory.HARMFUL, 0x2bb7cc));
    StuckEffect SLIMED = registerStuckEffects("slimed", new StuckEffect(StatusEffectCategory.HARMFUL, 0x42d408));
    WellRestedEffect WELL_RESTED = register("well_rested", new WellRestedEffect(StatusEffectCategory.BENEFICIAL, 0xd48208));
    DeferredInstantEffect DEFERRED_HEALTH = register("deferred_health", new DeferredInstantEffect(StatusEffectCategory.BENEFICIAL, (InstantStatusEffect) StatusEffects.INSTANT_HEALTH, 16262179));
    DeferredInstantEffect DEFERRED_DAMAGE = register("deferred_damage", new DeferredInstantEffect(StatusEffectCategory.HARMFUL, (InstantStatusEffect) StatusEffects.INSTANT_DAMAGE, 11101546));

    private static <T extends StatusEffect> T register(String name, T statusEffect) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }

    private static <T extends StuckEffect> T registerStuckEffects(String name, T statusEffect) {
        STUCK_EFFECTS.add(statusEffect);
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }

    static void initialize() {
        // static initialisation
    }
}
