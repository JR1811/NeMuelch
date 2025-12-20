package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

public class AdvancedFogBlockItem extends BlockItem {
    public AdvancedFogBlockItem(Settings settings) {
        super(NeMuelchBlocks.ADVANCED_FOG, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.isOf(this.getBlock())) return super.useOnBlock(context);
        if (player == null || !player.isSneaking()) return super.useOnBlock(context);

        if (world.getBlockEntity(blockPos) instanceof AdvancedFogBlockEntity blockEntity) {
            return blockEntity.openScreen(player) ? ActionResult.success(world.isClient) : ActionResult.FAIL;
        }

        return ActionResult.success(player instanceof ClientPlayerEntity);
    }
}
