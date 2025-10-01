package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;

public record BlightIngredients(@Nullable StatusEffect effect, @Nullable Ingredient blightIngredients) {
    public BlightIngredients(StatusEffect effect) {
        this(effect, null);
    }

    public BlightIngredients(Ingredient ingredient) {
        this(null, ingredient);
    }

    public BlightIngredients() {
        this(null, null);
    }

    public boolean matches(StatusEffect effect) {
        if (this.effect == null) return false;
        return this.effect.equals(effect);
    }

    public boolean matches(Ingredient blightIngredients) {
        if (this.blightIngredients == null) return false;
        return this.blightIngredients.equals(blightIngredients);
    }

    public boolean matches(ItemStack stack) {
        if (this.blightIngredients != null) {
            if (matches(Ingredient.ofStacks(stack))) return true;
        }
        if (this.effect == null) return false;
        for (StatusEffectInstance potionEffect : PotionUtil.getPotionEffects(stack)) {
            if (matches(potionEffect.getEffectType())) return true;
        }
        return false;
    }
}
