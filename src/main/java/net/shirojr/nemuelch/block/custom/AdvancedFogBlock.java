package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AdvancedFogBlock extends BlockWithEntity {
    public AdvancedFogBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedFogBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return super.onUse(state, world, pos, player, hand, hit);
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(this.asItem())) return super.onUse(state, world, pos, player, hand, hit);





        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.ADVANCED_FOG, AdvancedFogBlockEntity::tick);
    }
}
