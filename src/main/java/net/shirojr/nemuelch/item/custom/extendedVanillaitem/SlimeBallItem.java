package net.shirojr.nemuelch.item.custom.extendedVanillaitem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.SlimeItemEntity;

public class SlimeBallItem extends Item {
    public SlimeBallItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.NEUTRAL,
                0.5f, 0.75f);
        if (!world.isClient) {
            SlimeItemEntity slimeBallEntity = new SlimeItemEntity(world, user);
            slimeBallEntity.setItem(itemStack);
            slimeBallEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 0.65f, 3.0f);
            world.spawnEntity(slimeBallEntity);
        }
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
