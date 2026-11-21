package net.shirojr.nemuelch.screen.util;

import net.minecraft.inventory.Inventory;

public interface InventoryHolder<T extends Inventory> {
    T getInventory();
}
