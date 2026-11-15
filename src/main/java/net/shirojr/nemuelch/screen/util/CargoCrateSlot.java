package net.shirojr.nemuelch.screen.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;

public class CargoCrateSlot extends Slot {
    private final CargoCrateInventory cargoCrateInventory;

    public CargoCrateSlot(CargoCrateInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.cargoCrateInventory = inventory;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return playerEntity.isCreative() || playerEntity.isSpectator();
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return super.canInsert(stack) && this.cargoCrateInventory.canInsert(stack);
    }
}
