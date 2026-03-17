package net.shirojr.nemuelch.item.custom.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;

public class CrateBlockItem extends BlockItem {
    public CrateBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState replaceState = world.getBlockState(pos);
        if (!(replaceState.getBlock() instanceof CrateBlock)) return super.useOnBlock(context);
        if (!this.getBlock().equals(replaceState.getBlock())) return super.useOnBlock(context);
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isSneaking()) return super.useOnBlock(context);
        if (CrateBlock.changeType(world, pos, CrateBlock.Type.DOUBLE)) {
            if (!player.isCreative()) {
                context.getStack().decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}
