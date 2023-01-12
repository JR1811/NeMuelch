package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.screen.RopeWinchScreenHandler;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.jetbrains.annotations.Nullable;


public class RopeWinchBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private final PropertyDelegate propertyDelegate;
    private int progress = 0;
    private int maxProgress = 64;   // sets progress time
    private int markEjected = 0;

    public RopeWinchBlockEntity(BlockPos pos, BlockState state) {

        super(NeMuelchBlockEntities.ROPER_STATION, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> RopeWinchBlockEntity.this.progress;
                    case 1 -> RopeWinchBlockEntity.this.maxProgress;
                    case 2 -> RopeWinchBlockEntity.this.markEjected;    // 0 = false | 1 = true
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> RopeWinchBlockEntity.this.progress = value;
                    case 1 -> RopeWinchBlockEntity.this.maxProgress = value;
                    case 2 -> RopeWinchBlockEntity.this.markEjected = value;
                }
            }

            public int size() {
                return 2;
            }
        };
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

     public static void tick(World world, BlockPos blockPos, BlockState blockState, RopeWinchBlockEntity entity) {

        if (world.isClient()) { return; }

        entity.progress = entity.inventory.get(0).getCount();
        entity.markEjected = entity.propertyDelegate.get(2);

        if (entity.markEjected == 1) {
            entity.ejectItems();
            entity.setStack(0, ItemStack.EMPTY);
        }

        world.setBlockState(blockPos, blockState.with(RopeWinchBlock.ROPED, entity.progress > 0), Block.NOTIFY_ALL);

        markDirty(world, blockPos, blockState);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    public void ejectItems() {
        this.progress = 0;
        this.markEjected = 0;
        this.propertyDelegate.set(2, 0);
        this.propertyDelegate.set(0, 0);
        ItemScatterer.spawn(this.world, this.getPos(), this.inventory);
        this.removeStack(0);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.nemuelch.roper_station_gui_title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new RopeWinchScreenHandler(syncId, inv, this, this.propertyDelegate, ScreenHandlerContext.create(this.getWorld(), this.getPos()));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("roper_station.progress", progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
        progress = nbt.getInt("roper_station.progress");
    }

    private static int getRopeBlockCount(World world, BlockPos stationPos) {
        Direction stationDirection;
        int ropeCount = 0;
        if (world.getBlockState(stationPos) == null) { return ropeCount; }
        stationDirection = world.getBlockState(stationPos).get(RopeWinchBlock.FACING);
        BlockPos ropePos = stationPos.offset(stationDirection, 1).down();

        while (world.getBlockState(ropePos).getBlock() == NeMuelchBlocks.ROPE) {
            ropeCount++;
            ropePos.down();
        }
        return ropeCount;
    }

    private static void setRopeBlocks(World world, BlockPos stationPos, int ropeAmount) {
        if (world.isClient()) return;

        Direction stationDirection = world.getBlockState(stationPos).get(RopeWinchBlock.FACING);

        BlockPos ropePos = stationPos.mutableCopy().offset(stationDirection, 1).down();
        boolean isValidPos = world.canSetBlock(ropePos) && world.getBlockState(ropePos).getBlock() == Blocks.AIR;

        for (int i = 0; i < ropeAmount && isValidPos; i++) {
            if (i == 0) {
                world.setBlockState(ropePos, NeMuelchBlocks.ROPE.getDefaultState()
                        .with(RopeBlock.IS_END, true)
                        .with(RopeBlock.IS_ANCHOR, true),
                        Block.NOTIFY_LISTENERS);
            } else {
                world.setBlockState(ropePos.up(), NeMuelchBlocks.ROPE.getDefaultState().with(RopeBlock.IS_END, false),
                        Block.NOTIFY_LISTENERS);
                world.setBlockState(ropePos, NeMuelchBlocks.ROPE.getDefaultState().with(RopeBlock.IS_END, true),
                        Block.NOTIFY_LISTENERS);
            }

            ropePos = ropePos.down();
        }
    }

    private static boolean isRopeStack(RopeWinchBlockEntity entity) {
        Item item = entity.getStack(0).getItem();
        return Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(item).get()).isIn(NeMuelchTags.Items.ROPER_ROPES);
    }
}
