package net.shirojr.nemuelch.screen.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.screen.util.CargoCrateSlot;
import org.jetbrains.annotations.Nullable;

public class CargoCrateScreenHandler extends ScreenHandler {
    private static final int CRATE_SLOT_COUNT = 3;

    private final ScreenHandlerContext context;
    private final PropertyDelegate propertyDelegate;
    private final CargoCrateInventory inventory;


    public CargoCrateScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(syncId, playerInventory,
                new CargoCrateInventory(CargoCrateBlockEntity.INVENTORY_STACKS_AMOUNT, CargoCrateInventory.NO_OP_MARK_DIRTY),
                context, new ArrayPropertyDelegate(2), null
        );
    }

    public CargoCrateScreenHandler(int syncId, PlayerInventory playerInventory, CargoCrateInventory inventory,
                                   ScreenHandlerContext context, PropertyDelegate delegate, @Nullable ScreenHandlerListener listener) {
        super(NeMuelchScreenHandlers.CARGO_CRATE, syncId);
        this.inventory = inventory;
        this.context = context;
        inventory.onOpen(playerInventory.player);
        this.propertyDelegate = delegate;
        this.addProperties(this.propertyDelegate);
        if (listener != null) this.addListener(listener);

        int slotX = 58;
        int slotY = 83;
        for (int i = 0; i < CRATE_SLOT_COUNT; i++) {
            this.addSlot(new CargoCrateSlot(this.inventory, i, slotX, slotY));
            slotX += 22;
        }

        // player inv
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
            }
        }

        // player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }
    }

    public double getNormalizedProgress() {
        if (getMaxProgress() == 0) return 0;
        return MathHelper.clamp((double) this.getProgress() / this.getMaxProgress(), 0.0, 1);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public Text getStoredMaterial() {
        Text material = this.inventory.getMaterial();
        return material == null ? Text.translatable("container.nemuelch.cargo_crate.empty") : material;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack() && this.canInsertIntoSlot(slot)) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < CRATE_SLOT_COUNT) {
                if (!this.insertItem(originalStack, CRATE_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (!this.insertItem(originalStack, 0, CRATE_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
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
        return canUse(this.context, player, NeMuelchBlocks.CARGO_CRATE);
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return this.player().isCreative() || this.player().isSpectator();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!(player instanceof ServerPlayerEntity)) return;
        this.inventory.sort();
    }
}
