package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.LivingEntity;
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
        return super.finishUsing(stack, world, user);
    }
}
