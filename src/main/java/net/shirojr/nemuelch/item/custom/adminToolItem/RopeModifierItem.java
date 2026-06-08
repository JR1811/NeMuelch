package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.item.client.RopeModificationHandler;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;

public class RopeModifierItem extends Item implements ThirdPersonInvisible {
    public RopeModifierItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            boolean openedScreen = RopeModificationHandler.attemptScreenOpening();
            if (openedScreen) {
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }

    public static boolean canInteract(ServerPlayerEntity player, ItemStack stack, RopeData ropeData, double distance) {
        if (!(stack.getItem() instanceof RopeModifierItem)) return false;
        if (player.isCreative() || player.hasPermissionLevel(2)) return true;
        return player.squaredDistanceTo(ropeData.pointA()) <= distance * distance ||
                player.squaredDistanceTo(ropeData.pointB()) <= distance * distance;
    }
}
