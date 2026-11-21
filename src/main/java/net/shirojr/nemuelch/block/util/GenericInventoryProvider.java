package net.shirojr.nemuelch.block.util;

import net.minecraft.inventory.Inventory;

public interface GenericInventoryProvider<T extends Inventory> {
    T getInventory();
}
