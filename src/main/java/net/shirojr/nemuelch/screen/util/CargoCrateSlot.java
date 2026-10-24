package net.shirojr.nemuelch.screen.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class CargoCrateSlot extends Slot {
    public CargoCrateSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return playerEntity.isCreative() || playerEntity.isSpectator();
    }
}
