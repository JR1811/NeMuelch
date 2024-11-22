package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.DropPotBlockEntity;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import org.jetbrains.annotations.Nullable;

public class DropPotBlock extends BlockWithEntity implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public DropPotBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DropPotBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!(world.getBlockEntity(hit.getBlockPos()) instanceof DropPotBlockEntity blockEntity)) {
            return super.onUse(state, world, pos, player, hand, hit);
        }
        if (player.isSneaking()) {
            if (world instanceof ServerWorld serverWorld) {
                ItemStack potStack = DropPotBlockItem.withInventory(blockEntity.getItems());
                ItemScatterer.spawn(serverWorld, pos.getX(), pos.getY(), pos.getZ(), potStack);
                blockEntity.setShouldDropContent(false);
                serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
                serverWorld.playSound(null, pos, this.soundGroup.getBreakSound(), SoundCategory.BLOCKS, 2f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        if (!stack.isEmpty()) {
            if (world instanceof ServerWorld serverWorld) {
                if (blockEntity.tryAddingStack(stack)) {
                    if (!player.isCreative()) stack.decrement(stack.getCount());
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 2f, 1f);
                    serverWorld.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_BRICKS_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
            } else if (blockEntity.canAddStack(stack)) {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.DROP_BLOCK, DropPotBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape bottom = Block.createCuboidShape(4, 0, 4, 12, 1, 12);
        VoxelShape base = Block.createCuboidShape(1, 1, 1, 15, 11, 15);
        VoxelShape top = Block.createCuboidShape(4, 11, 4, 12, 16, 12);
        return VoxelShapes.union(base, top, bottom);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof DropPotBlockEntity blockEntity) {
            if (world.getBlockState(pos.down()).isAir() && world.getBlockState(pos.up()).isAir()) {
                blockEntity.setShouldDropContent(false);
                serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
                DropPotEntity potEntity = new DropPotEntity(serverWorld, Vec3d.ofCenter(pos), Vec3d.ZERO, blockEntity.getItems());
                serverWorld.spawnEntity(potEntity);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.getBlock().equals(newState.getBlock()) && world.getBlockEntity(pos) instanceof DropPotBlockEntity blockEntity) {
            if (blockEntity.shouldDropContent()) {
                blockEntity.dropInventoryAndClear();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
