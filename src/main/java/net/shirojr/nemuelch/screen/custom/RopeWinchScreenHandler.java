package net.shirojr.nemuelch.screen.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.block.entity.RopeWinchBlockEntity;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.util.NeMuelchTags;

public class RopeWinchScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PlayerEntity player;
    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;

    public RopeWinchScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(1), new ArrayPropertyDelegate(3), ScreenHandlerContext.EMPTY);
    }

    public RopeWinchScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate, ScreenHandlerContext context) {
        super(NeMuelchScreenHandlers.ROPER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.onOpen(player);
        this.propertyDelegate = delegate;
        this.context = context;


        this.addSlot(new Slot(inventory, 0, 80, 21) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return canInsertIntoSlot(stack, slots.get(0));
            }
            public void markDirty() {
                RopeWinchScreenHandler.this.onContentChanged(this.inventory);
                super.markDirty();
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(delegate);
    }

    public boolean isRoping() {
        return propertyDelegate.get(0) > 0;
    }

    // executed by button press in the block's screen class
    public void resetProgress() {
        if (player.getWorld().isClient()) {
            this.propertyDelegate.set(0, 0);    // resets progress arrow on client side
            this.player.playSound(SoundEvents.ENTITY_LEASH_KNOT_BREAK, 2f, 1f);
        }

        //executed on server side from onButtonClick() method
        this.context.run((world, pos) -> {
            ItemScatterer.spawn(world, pos.up(), this.inventory);
            this.inventory.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
        });
    }

    public void applyProgress() {
        if (player.getWorld().isClient()) {
            this.player.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 2f, 1f);
        }

        this.context.run((world, pos) -> {
            RopeWinchBlockEntity.setRopeBlocks(world, pos, inventory.getStack(0).getCount());
            this.inventory.removeStack(0, RopeWinchBlockEntity.getValidRopeBlockSpace(world, pos));
            this.sendContentUpdates();
        });
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0) {
            // eject button
            this.resetProgress();
            return true;
        }

        if (id == 1) {
            // unroll button
            this.applyProgress();
            return true;
        }
        return super.onButtonClick(player, id);
    }

    public int getScaledProgress() {

        int progress = this.propertyDelegate.get(0);
        int maxProgress = this.propertyDelegate.get(1);
        int progressArrowSize = 32;

        if (maxProgress == 0 || progress == 0) return 0;
        return (progress * progressArrowSize)  / maxProgress;
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
        return canUse(this.context, player, NeMuelchBlocks.ROPER);
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
        return Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(itemStack.getItem()).get()).isIn(NeMuelchTags.Items.ROPER_ROPES);
    }
}
