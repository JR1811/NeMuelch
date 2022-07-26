package net.shirojr.nemuelch.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.util.registry.Registry;

public class NeMuelchDrinkComponents {

    //not used anyways and the references might not be called (besides the green muelch one...)
    public static final FoodComponent GREEN_MILK = new FoodComponent.Builder().hunger(4).saturationModifier(0.4F).alwaysEdible().build();
    public static final FoodComponent BLUE_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodComponent BROWN_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodComponent PINK_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodComponent YELLOW_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodComponent PURPLE_MILK = new FoodComponent.Builder().hunger(4).saturationModifier(0.4F).alwaysEdible().build();

}