package net.shirojr.nemuelch.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.NeMuelchTags;

public class PestcaneStationScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public PestcaneStationScreenHandler(int syncId, PlayerInventory inventory) {

        this(syncId, inventory, new SimpleInventory(3), new ArrayPropertyDelegate(2));
    }

    public PestcaneStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {

        super(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        this.propertyDelegate = delegate;

        this.addSlot(new Slot(inventory, 0, 56, 17));
        this.addSlot(new Slot(inventory, 1, 56, 53));
        this.addSlot(new Slot(inventory, 2, 116, 35));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(delegate);
    }

    public boolean isCrafting() {
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledProgress() {

        int progress = this.propertyDelegate.get(0);
        int maxProgress = this.propertyDelegate.get(1);
        int progressArrowSize = 23;

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {

        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {

            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.size()) {

                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {

                    return ItemStack.EMPTY;
                }

            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {

                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {

                slot.setStack(ItemStack.EMPTY);

            } else {

                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {

                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack itemStack, Slot slot) {

        return switch (slot.getIndex()) {
            case 0 ->     // top slot
                    Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(itemStack.getItem()).get()).isIn(NeMuelchTags.Items.PESTCANE_UPGRADE_MATERIAL);
            case 1 ->     // bottom slot
                    Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(itemStack.getItem()).get()).isIn(NeMuelchTags.Items.PESTCANES);
            case 2 ->     // output slot
                    false;
            default ->    // any other slot
                    true;
        };
    }
}
