package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchFoodComponents;

public class RottenMeatAppleItem extends Item {
    public RottenMeatAppleItem(Settings settings) {
        super(settings.food(NeMuelchFoodComponents.MEAT_FRUIT));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack finishedStack = super.finishUsing(stack, world, user);
        int amplifier = 0;
        if (user.getHealth() > (amplifier + 1) * 6 || user.isUndead()) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, amplifier));
        } else {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 3));
        }
        return finishedStack;
    }
}
