package net.shirojr.nemuelch.item;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.MilkBucketItem;

public class ModFoodComponents {

    public static final FoodComponent GREEN_MILK = new FoodComponent.Builder().hunger(4).saturationModifier(0.4F).alwaysEdible().build();
    public static final FoodComponent BLUE_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 2400, 0), 1.0F).alwaysEdible().build();
    public static final FoodComponent BROWN_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 500, 2), 1.0F).alwaysEdible().build();
    public static final FoodComponent PINK_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 700, 0), 1.0F).alwaysEdible().build();
    public static final FoodComponent YELLOW_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 100, 3), 1.0F).alwaysEdible().build();

}
