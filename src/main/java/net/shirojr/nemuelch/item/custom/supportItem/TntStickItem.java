package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;

public class TntStickItem extends Item {
    public TntStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getMainHandStack();
        ItemStack offStack = user.getOffHandStack();

        if (!(offStack.getItem() instanceof FlintAndSteelItem)) return super.use(world, user, hand);
        if (world.isClient()) return TypedActionResult.success(user.getStackInHand(hand), true);

        TntStickItemEntity tntStickItemEntity = new TntStickItemEntity(world, user);
        tntStickItemEntity.setItem(stack);
        tntStickItemEntity.setTickCount(100);
        tntStickItemEntity.setMaxBounces(4);
        tntStickItemEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.2f, 1.0f);
        world.spawnEntity(tntStickItemEntity);

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
            offStack.damage(1, user, p -> p.sendToolBreakStatus(Hand.OFF_HAND));
        }

        return TypedActionResult.success(user.getStackInHand(hand), true);
    }
}
