package net.shirojr.nemuelch.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.custom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NeMuelchEffects {
    public static StatusEffect SHIELDING_SKIN = register("shielding_skin", new ShieldingSkinEffect(StatusEffectCategory.BENEFICIAL, 3124687));
    public static StatusEffect PLAYTHING_OF_THE_UNSEEN_DEITY = register("plaything_of_the_unseen_deity", new PlaythingOfTheUnseenDeityEffect(StatusEffectCategory.HARMFUL, 3124687));
    public static StatusEffect LEVITATING_ABSOLUTION = register("levitating_absolution", new LevitatingAbsolutionEffect(StatusEffectCategory.NEUTRAL, 111111));
    public static StatusEffect STUCK_DEFAULT = register("stuck", new StuckEffect(StatusEffectCategory.HARMFUL, 0x2bb7cc));
    public static StatusEffect SLIMED = register("slimed", new StuckEffect(StatusEffectCategory.HARMFUL, 0x42d408));
    public static StatusEffect WELL_RESTED = register("well_rested", new WellRestedEffect(StatusEffectCategory.BENEFICIAL, 0xd48208));
    public static StatusEffect OBFUSCATED = register("obfuscated", new ObfuscatedEffect(StatusEffectCategory.BENEFICIAL, 0x192107));

    public static List<StatusEffect> STUCK_EFFECTS = new ArrayList<>();

    public static StatusEffect register(String name, StatusEffect statusEffect) {
        return Registry.register(Registry.STATUS_EFFECT, new Identifier(NeMuelch.MOD_ID, name), statusEffect);
    }

    public static void init() {
        STUCK_EFFECTS.addAll(Arrays.asList(STUCK_DEFAULT, SLIMED));
    }
}
