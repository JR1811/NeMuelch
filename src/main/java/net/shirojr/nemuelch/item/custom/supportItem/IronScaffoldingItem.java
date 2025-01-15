package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.IronScaffoldingBlock;
import org.jetbrains.annotations.Nullable;

public class IronScaffoldingItem extends BlockItem {

    public IronScaffoldingItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Nullable
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = this.getBlock();
        if (!blockState.isOf(block)) {
            return IronScaffoldingBlock.calculateDistance(world, blockPos) == IronScaffoldingBlock.MAX_DISTANCE ? null : context;
        } else {
            Direction direction;
            if (context.shouldCancelInteraction()) {
                direction = context.hitsInsideBlock() ? context.getSide().getOpposite() : context.getSide();
            } else {
                direction = context.getSide() == Direction.UP ? context.getPlayerFacing() : Direction.UP;
            }

            int i = 0;
            BlockPos.Mutable mutable = blockPos.mutableCopy().move(direction);

            while(i < IronScaffoldingBlock.MAX_DISTANCE) {
                if (!world.isClient && !world.isInBuildLimit(mutable)) {
                    PlayerEntity playerEntity = context.getPlayer();
                    int j = world.getTopY();
                    if (playerEntity instanceof ServerPlayerEntity && mutable.getY() >= j) {
                        ((ServerPlayerEntity)playerEntity).sendMessage((new TranslatableText("build.tooHigh", j - 1)).formatted(Formatting.RED), MessageType.GAME_INFO, Util.NIL_UUID);
                    }
                    break;
                }

                blockState = world.getBlockState(mutable);
                if (!blockState.isOf(this.getBlock())) {
                    if (blockState.canReplace(context)) {
                        return ItemPlacementContext.offset(context, mutable, direction);
                    }
                    break;
                }

                mutable.move(direction);
                if (direction.getAxis().isHorizontal()) {
                    ++i;
                }
            }

            return null;
        }
    }

    @Override
    protected boolean checkStatePlacement() {
        return false;
    }
}
