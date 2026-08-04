package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;

public class CargoCrateBlockEntity extends BlockEntity {
    private static final int BARREL_COUNT = 25;
    private static final int SLOT_PER_BARREL_COUNT = 27;

    private final CargoCrateInventory inventory;

    public CargoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CARGO_CRATE, pos, state);
        this.inventory = new CargoCrateInventory((int) (SLOT_PER_BARREL_COUNT * BARREL_COUNT * 1.5), this::markDirty);
    }

    public CargoCrateInventory getInventory() {
        return inventory;
    }

    public void dropInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.pos, this.getInventory().getStacks());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtCompound inventoryNbt = new NbtCompound();
        Inventories.readNbt(inventoryNbt, this.inventory.getStacks());
        nbt.put(NeMuelchNbtKeys.INVENTORY, inventoryNbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound inventoryNbt = nbt.getCompound(NeMuelchNbtKeys.INVENTORY);
        Inventories.writeNbt(inventoryNbt, this.inventory.getStacks());
    }
}
