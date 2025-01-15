package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import net.shirojr.nemuelch.util.HandledInventory;

public class DropPotBlockEntity extends BlockEntity implements HandledInventory {
    public static final int SLOT_SIZE = 9;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(SLOT_SIZE, ItemStack.EMPTY);
    private boolean shouldDropContent = true;

    public DropPotBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.DROP_BLOCK, pos, state);
    }

    public boolean shouldDropContent() {
        return shouldDropContent;
    }

    public void setShouldDropContent(boolean shouldDropContent) {
        this.shouldDropContent = shouldDropContent;
    }

    public ItemStack asItemStack() {
        return DropPotBlockItem.withInventory(this.inventory);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    public void dropInventoryAndClear() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(world, pos, getItems());
        this.clear();
    }

    public static void tick(World world, BlockPos pos, BlockState state, DropPotBlockEntity blockEntity) {

    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, getItems());
        if (nbt.contains("dropContent")) {
            setShouldDropContent(nbt.getBoolean("dropContent"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, getItems());
        nbt.putBoolean("dropContent", shouldDropContent());
    }
}
