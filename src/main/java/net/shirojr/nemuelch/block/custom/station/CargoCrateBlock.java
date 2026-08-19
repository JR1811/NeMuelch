package net.shirojr.nemuelch.block.custom.station;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockPattern;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("deprecation")
public class CargoCrateBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final IntProperty OFFSET_X = IntProperty.of("offset_x", 0, 2);
    public static final IntProperty OFFSET_Y = IntProperty.of("offset_y", 0, 2);
    public static final IntProperty OFFSET_Z = IntProperty.of("offset_z", 0, 2);

    private static final NeMuelchBlockPattern CONVERSION_PATTERN = NeMuelchBlockPattern.CARGO_CRATE;

    public CargoCrateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(OFFSET_X, 0)
                .with(OFFSET_Y, 0)
                .with(OFFSET_Z, 0)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            breakStructure(world, state, pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public static void attemptConversion(WorldView world, BlockPos pos, LivingEntity placer) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!placer.isSneaking()) return;
        Collection<CachedBlockPosition> entries = CONVERSION_PATTERN.getEntries(world, pos);
        Optional<BlockPos> optionalCorePos = Optional.ofNullable(CONVERSION_PATTERN.getCore(world, pos)).map(CachedBlockPosition::getBlockPos);
        if (entries == null || optionalCorePos.isEmpty()) return;
        Direction placementDirection = null;
        BlockPos corePos = optionalCorePos.get();
        for (CachedBlockPosition entry : entries) {
            if (!(entry.getBlockEntity() instanceof Inventory)) return;
            BlockState state = entry.getBlockState();
            if (!entry.getBlockPos().equals(corePos)) {
                if (!state.contains(FACING)) return;
                if (placementDirection != null && state.get(FACING) != placementDirection) return;
                placementDirection = state.get(FACING);
            }
        }
        if (placementDirection == null) return;

        List<ItemStack> stackContent = new ArrayList<>();
        List<ItemStack> originalBlocks = new ArrayList<>();
        for (CachedBlockPosition entry : entries) {
            if (entry.getBlockEntity() instanceof Inventory inventory) {
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack entryStack = inventory.getStack(i);
                    if (entryStack.isEmpty()) continue;
                    stackContent.add(entryStack.copy());
                }
                inventory.clear();
            }
            originalBlocks.add(entry.getBlockState().getBlock().asItem().getDefaultStack());
            serverWorld.setBlockState(entry.getBlockPos(), Blocks.AIR.getDefaultState());
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos partPos = corePos.add(dx, dy, dz);
                    BlockState newState = NeMuelchBlocks.CARGO_CRATE.getDefaultState()
                            .with(CargoCrateBlock.FACING, placementDirection)
                            .with(CargoCrateBlock.OFFSET_X, dx + 1)
                            .with(CargoCrateBlock.OFFSET_Y, dy + 1)
                            .with(CargoCrateBlock.OFFSET_Z, dz + 1);
                    serverWorld.setBlockState(partPos, newState);
                }
            }
        }

        if (serverWorld.getBlockEntity(corePos) instanceof CargoCrateBlockEntity blockEntity) {
            List<ItemStack> leftOverStacks = blockEntity.getInventory().insertStacks(stackContent);
            for (ItemStack leftOverStack : leftOverStacks) {
                ItemScatterer.spawn(serverWorld, corePos.getX(), corePos.getY(), corePos.getZ(), leftOverStack);
            }
            blockEntity.setOriginalBlocksStacks(originalBlocks);
            blockEntity.markDirty();
        }
    }

    private static void breakStructure(World world, BlockState oldState, BlockPos changedPos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        int offsetX = oldState.get(OFFSET_X);
        int offsetY = oldState.get(OFFSET_Y);
        int offsetZ = oldState.get(OFFSET_Z);
        BlockPos corePos = changedPos.add(-(offsetX - 1), -(offsetY - 1), -(offsetZ - 1));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos entryPos = corePos.add(dx, dy, dz);
                    if (entryPos.equals(changedPos)) continue;
                    BlockState partState = world.getBlockState(entryPos);
                    if (!(partState.getBlock() instanceof CargoCrateBlock)) continue;
                    if (dx == 0 && dy == 0 && dz == 0) {
                        if (world.getBlockEntity(entryPos) instanceof CargoCrateBlockEntity blockEntity) {
                            blockEntity.dropInventory();
                        }
                    }
                    serverWorld.setBlockState(entryPos, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public static boolean isValidCoreState(BlockState state) {
        return state.isIn(ConventionalBlockTags.CHESTS) && state.contains(Properties.HORIZONTAL_FACING);
    }

    public static boolean isValidWallState(BlockState state) {
        return state.isIn(ConventionalBlockTags.WOODEN_BARRELS) && state.contains(Properties.FACING);
    }

    @SuppressWarnings("unused")
    @Nullable
    public static CachedBlockPosition getCore(WorldView world, BlockPos pos) {
        return CONVERSION_PATTERN.getCore(world, pos);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (Part.get(state) != Part.CENTER) return null;
        return new CargoCrateBlockEntity(pos, state);
    }

    public enum Part implements StringIdentifiable {
        CORNER, EDGE, FACE, CENTER;

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static Part get(int offsetX, int offsetY, int offsetZ) {
            int extremes = (offsetX != 1 ? 1 : 0) + (offsetY != 1 ? 1 : 0) + (offsetZ != 1 ? 1 : 0);
            return switch (extremes) {
                case 0 -> CENTER;
                case 1 -> FACE;
                case 2 -> EDGE;
                default -> CORNER;
            };
        }

        @Nullable
        public static Part get(BlockState state) {
            if (!state.contains(OFFSET_X) || !state.contains(OFFSET_Y) || !state.contains(OFFSET_Z)) return null;
            return get(state.get(OFFSET_X), state.get(OFFSET_Y), state.get(OFFSET_Z));
        }
    }
}
