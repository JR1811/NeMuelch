package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.WandOfSolBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

public class WandOfSolBlock extends BlockWithEntity {
    public static final IntProperty STATE = NeMuelchProperties.WAND_OF_SOL_STATE;

    public WandOfSolBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(STATE, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WandOfSolBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0.3, 0, 0.3, 0.7, 1.0, 0.7);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos().mutableCopy();
        World world = ctx.getWorld();

        if (!world.isInBuildLimit(pos.up(2))) return null;
        if (world.getBlockState(pos).canReplace(ctx) && world.getBlockState(pos.down()).canReplace(ctx)) return null;

        return this.getDefaultState().with(STATE, 0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }
}
