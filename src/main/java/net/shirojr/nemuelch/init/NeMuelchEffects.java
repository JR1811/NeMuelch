package net.shirojr.nemuelch.init;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.custom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NeMuelchEffects {
    public static List<StatusEffect> STUCK_EFFECTS = new ArrayList<>();

    public static ShieldingSkinEffect SHIELDING_SKIN = register("shielding_skin", new ShieldingSkinEffect(StatusEffectCategory.BENEFICIAL, 3124687));
    public static PlaythingOfTheUnseenDeityEffect PLAYTHING_OF_THE_UNSEEN_DEITY = register("plaything_of_the_unseen_deity", new PlaythingOfTheUnseenDeityEffect(StatusEffectCategory.HARMFUL, 3124687));
    public static LevitatingAbsolutionEffect LEVITATING_ABSOLUTION = register("levitating_absolution", new LevitatingAbsolutionEffect(StatusEffectCategory.NEUTRAL, 111111));
    public static StuckEffect STUCK_DEFAULT = registerStuckEffects("stuck", new StuckEffect(StatusEffectCategory.HARMFUL, 0x2bb7cc));
    public static StuckEffect SLIMED = registerStuckEffects("slimed", new StuckEffect(StatusEffectCategory.HARMFUL, 0x42d408));
    public static WellRestedEffect WELL_RESTED = register("well_rested", new WellRestedEffect(StatusEffectCategory.BENEFICIAL, 0xd48208));
    public static ObfuscatedEffect OBFUSCATED = register("obfuscated", new ObfuscatedEffect(StatusEffectCategory.BENEFICIAL, 0x192107));

    private static <T extends StatusEffect> T register(String name, T statusEffect) {
        return Registry.register(Registry.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }

    private static <T extends StuckEffect> T registerStuckEffects(String name, T statusEffect) {
        STUCK_EFFECTS.add(statusEffect);
        return Registry.register(Registry.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }

    public static void initialize() {
        // static initialisation
    }
}
