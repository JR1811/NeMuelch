package net.shirojr.nemuelch.mixin.access;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlockEntity.class)
public interface InventoryAccessor {

    @Accessor("inventory")
    public DefaultedList<ItemStack> nemuelch$getInventory ();
}
