package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;

public class PotLauncherItem extends Item {
    public PotLauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getBlockPos());
        if (Block.isFaceFullSquare(state.getCollisionShape(world, context.getBlockPos()), Direction.UP)) {
            if (world instanceof ServerWorld serverWorld) {
                PotLauncherEntity entity;
                if (context.getPlayer() == null) {
                    entity = new PotLauncherEntity(serverWorld, context.getHitPos());
                } else {
                    entity = new PotLauncherEntity(serverWorld, context.getHitPos(), 0.0f, context.getPlayer().getYaw());
                }
                serverWorld.spawnEntity(entity);
                serverWorld.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 2.0f, 1.0f);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}
