package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;

@SuppressWarnings("deprecation")
public class SmallFenceBlock extends HorizontalConnectingBlock implements VariationHolder {
    private final VoxelShape[] cullingShapes;
    private final Variation variation;

    public SmallFenceBlock(Settings settings, Variation variation) {
        super(2f, 2f, 8f, 8f, 8f, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));
        this.cullingShapes = this.createShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
        this.variation = variation;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.cullingShapes[this.getShapeIndex(state)];
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getOutlineShape(state, world, pos, context);
    }

    @Override
    public Variation getVariant() {
        return this.variation;
    }

    @Override
    public Block getBlock() {
        return this;
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_small_fence");
    }
}
