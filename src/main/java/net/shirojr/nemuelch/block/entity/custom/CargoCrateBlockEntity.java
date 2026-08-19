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
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;

import java.util.List;

public class CargoCrateBlockEntity extends BlockEntity {
    private static final int STACK_PER_BLOCK_COUNT = 27;

    private final DefaultedList<ItemStack> originalBlocks;
    private final CargoCrateInventory inventory;

    public CargoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CARGO_CRATE, pos, state);
        this.originalBlocks = DefaultedList.ofSize(3 * 3 * 3, ItemStack.EMPTY);
        this.inventory = new CargoCrateInventory((int) (STACK_PER_BLOCK_COUNT * originalBlocks.size() * 1.5), this::markDirty);
    }

    public void setOriginalBlocksStacks(List<ItemStack> originalBlocksStacks) {
        for (int i = 0; i < originalBlocksStacks.size() && i < originalBlocks.size(); i++) {
            ItemStack blockStack = originalBlocksStacks.get(i);
            if (blockStack.isEmpty()) continue;
            this.originalBlocks.set(i, blockStack);
        }
    }

    public CargoCrateInventory getInventory() {
        return inventory;
    }

    public void dropInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.pos, this.getInventory().getStacks());
        ItemScatterer.spawn(serverWorld, this.pos, this.originalBlocks);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.inventory.readNbt(nbt.getCompound(NeMuelchNbtKeys.INVENTORY));

        if (nbt.contains(NeMuelchNbtKeys.ORIGINAL)) {
            NbtCompound inventoryNbt = nbt.getCompound(NeMuelchNbtKeys.ORIGINAL);
            Inventories.readNbt(inventoryNbt, this.originalBlocks);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        this.inventory.writeNbt(nbt);

        NbtCompound originalBlocksNbt = new NbtCompound();
        Inventories.writeNbt(originalBlocksNbt, this.originalBlocks);
        nbt.put(NeMuelchNbtKeys.ORIGINAL, originalBlocksNbt);
    }
}
