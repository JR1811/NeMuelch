package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.screen.handler.RopeWinchScreenHandler;
import net.shirojr.nemuelch.util.ImplementedInventory;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;


public class RopeWinchBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private static int tick = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate;
    private int storedRopes = 0;
    private int maxRopeCount = 128;

    public RopeWinchBlockEntity(BlockPos pos, BlockState state) {

        super(NeMuelchBlockEntities.ROPER_STATION, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> RopeWinchBlockEntity.this.storedRopes;
                    case 1 -> RopeWinchBlockEntity.this.maxRopeCount;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> RopeWinchBlockEntity.this.storedRopes = value;
                    case 1 -> RopeWinchBlockEntity.this.maxRopeCount = value;
                }
            }

            public int size() {
                return 2;
            }
        };
    }

    public int getStoredRopes() {
        return this.storedRopes;
    }

    public void setStoredRopes(int count) {
        this.storedRopes = count;
        this.propertyDelegate.set(0, count);
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, RopeWinchBlockEntity entity) {
        if (world.isClient()) {
            return;
        }

        if (!(world.getBlockEntity(blockPos) instanceof RopeWinchBlockEntity)) {
            return;
        }
        if (entity.getStoredRopes() == entity.getCurrentRopeLength(blockPos)) {
            return;
        }

        tick++;
        if (tick % 30 != 0) {
            return;
        }

        // find rope pos
        BlockPos ropePos = getLastRopePos(world, blockPos);

        // add rope blocks if there are not enough deployed
        while (entity.getCurrentRopeLength(blockPos) < entity.getStoredRopes() && isValidRopeBlockPos(world, ropePos)) {
            if (entity.getCurrentRopeLength(blockPos) == 0) {
                world.setBlockState(ropePos, NeMuelchBlocks.ROPE
                                .getDefaultState().with(NeMuelchProperties.ROPE_ANCHOR, true),
                        Block.NOTIFY_LISTENERS);
            } else {
                world.setBlockState(ropePos, NeMuelchBlocks.ROPE
                                .getDefaultState().with(NeMuelchProperties.ROPE_ANCHOR, false),
                        Block.NOTIFY_LISTENERS);
            }

            ropePos = ropePos.down();
        }

        // remove rope blocks if there are too many deployed
        ropePos = ropePos.up();
        while (entity.getCurrentRopeLength(blockPos) > entity.getStoredRopes()) {
            world.removeBlock(ropePos, false);
            ropePos = ropePos.up();
        }

        tick = 0;
        world.setBlockState(blockPos, blockState.with(RopeWinchBlock.ROPED, entity.getCurrentRopeLength(blockPos) > 0), Block.NOTIFY_ALL);
        markDirty(world, blockPos, blockState);
    }

    /**
     * Finds last location of the rope blocks
     *
     * @param stationPos position of the station
     * @return location of the lowest rope block
     */
    public static BlockPos getLastRopePos(World world, BlockPos stationPos) {
        Direction stationDirection = world.getBlockState(stationPos).get(RopeWinchBlock.FACING);
        BlockPos ropePos = stationPos.mutableCopy().offset(stationDirection, 1).down();

        while (world.getBlockState(ropePos).getBlock() == NeMuelchBlocks.ROPE) {
            ropePos = ropePos.down();
        }

        return ropePos;
    }

    public static void removeAllRopeBlocks(World world, BlockPos stationPos) {
        if (world.isClient()) return;
        Direction stationDirection = world.getBlockState(stationPos).get(RopeWinchBlock.FACING);

        BlockPos ropePos = stationPos.mutableCopy().offset(stationDirection, 1).down();
        while (world.getBlockState(ropePos).getBlock() == NeMuelchBlocks.ROPE) {
            world.breakBlock(ropePos, false);
            ropePos = ropePos.down();
        }

        world.setBlockState(stationPos, world.getBlockState(stationPos)
                .with(RopeWinchBlock.ROPED, false), Block.NOTIFY_LISTENERS);
    }

    public static int getValidRopeBlockSpace(World world, BlockPos stationPos) {
        Direction stationDirection = world.getBlockState(stationPos).get(RopeWinchBlock.FACING);

        int ropeCount = 0;

        if (world.getBlockState(stationPos).getBlock() != NeMuelchBlocks.ROPER) {
            return ropeCount;
        }
        BlockPos ropePos = stationPos.offset(stationDirection, 1).down();

        while (isValidRopeBlockPos(world, ropePos)) {
            ropeCount++;
            ropePos = ropePos.down();
        }
        return ropeCount;
    }

    public static boolean isValidRopeBlockPos(World world, BlockPos ropePos) {
        return world.canSetBlock(ropePos) &&
                (world.getBlockState(ropePos).getBlock() == Blocks.AIR
                        || world.getBlockState(ropePos).getBlock() == NeMuelchBlocks.ROPE);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.nemuelch.roper_station_gui_title");
    }

    /**
     * Accounts for Station offset
     *
     * @param pos position of station
     * @return returns amount of deployed rope blocks
     */
    private int getCurrentRopeLength(BlockPos pos) {
        if (world == null || world.isClient()) return 0;

        // find rope location
        if (world.getBlockState(pos).getBlock() == NeMuelchBlocks.ROPER) {
            Direction stationDirection = world.getBlockState(pos).get(RopeWinchBlock.FACING);
            pos = pos.mutableCopy().offset(stationDirection, 1).down();
        }

        int length = 0;
        while (world.getBlockState(pos).getBlock() == NeMuelchBlocks.ROPE) {
            length++;
            pos = pos.down();
        }
        return length;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new RopeWinchScreenHandler(syncId, inv, this, this.propertyDelegate, ScreenHandlerContext.create(this.getWorld(), this.getPos()), storedRopes);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("roper_station.stored", storedRopes);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
        storedRopes = nbt.getInt("roper_station.stored");
    }

}
