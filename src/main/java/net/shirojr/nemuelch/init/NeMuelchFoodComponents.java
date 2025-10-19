package net.shirojr.nemuelch.init;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public interface NeMuelchFoodComponents {
    FoodComponent GREEN_MILK = new FoodComponent.Builder().hunger(4).saturationModifier(0.4F).alwaysEdible().build();
    FoodComponent BLUE_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    FoodComponent BROWN_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    FoodComponent PINK_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    FoodComponent YELLOW_MILK = new FoodComponent.Builder().hunger(8).saturationModifier(1.2F).alwaysEdible().build();
    FoodComponent PURPLE_MILK = new FoodComponent.Builder().hunger(4).saturationModifier(0.4F).alwaysEdible().build();
    FoodComponent MEAT_FRUIT = new FoodComponent.Builder().hunger(2).saturationModifier(0.1F).alwaysEdible()
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 4), 0.7f)
            .build();
}
