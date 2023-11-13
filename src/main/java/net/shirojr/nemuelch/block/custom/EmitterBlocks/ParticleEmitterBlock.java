package net.shirojr.nemuelch.block.custom.EmitterBlocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.block.entity.ParticleEmitterBlockEntity;
import net.shirojr.nemuelch.screen.ParticleEmitterBlockScreen;
import org.jetbrains.annotations.Nullable;

public class ParticleEmitterBlock extends BlockWithEntity implements Waterloggable, BlockEntityProvider {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;


    public ParticleEmitterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return NeMuelchBlockEntities.PARTICLE_EMITTER.instantiate(pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return context.isHolding(NeMuelchBlocks.PARTICLE_EMITTER.asItem()) ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = ctx.getWorld().getBlockState(blockPos);
        if (blockState.isOf(this)) {
            return blockState.with(WATERLOGGED, false);
        }
        FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
        return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }

        return super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().creativeMode) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;

        if (player.isSneaking()) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            player.openHandledScreen(screenHandlerFactory);
            return ActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof ParticleEmitterBlockEntity particleEmitterBlockEntity) {
            int newIndex = 0;
            var list = Registry.PARTICLE_TYPE;
            if (particleEmitterBlockEntity.getCurrentParticleId() != null) {
                newIndex = list.getRawId(list.get(particleEmitterBlockEntity.getCurrentParticleId())) + 1;
            }
            var particle = list.get(newIndex);
            particleEmitterBlockEntity.setCurrentParticleId(list.getId(particle));
            if (particle != null && list.getId(particle) != null) {
                Identifier id = list.getId(particle);
                if (id != null) {
                    String idString = id.toString();
                    player.sendMessage(new TranslatableText(idString), true);
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.PARTICLE_EMITTER, ParticleEmitterBlockEntity::tick);
    }
}



