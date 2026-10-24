package net.shirojr.nemuelch.screen.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.screen.util.CargoCrateSlot;

public class CargoCrateScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final PropertyDelegate propertyDelegate;
    private final CargoCrateInventory inventory;


    public CargoCrateScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(syncId, playerInventory,
                new CargoCrateInventory(CargoCrateBlockEntity.INVENTORY_STACKS_AMOUNT, CargoCrateInventory.NO_OP_MARK_DIRTY),
                context, new ArrayPropertyDelegate(6)
        );
    }

    public CargoCrateScreenHandler(int syncId, PlayerInventory playerInventory, CargoCrateInventory inventory,
                                   ScreenHandlerContext context, PropertyDelegate delegate) {
        super(NeMuelchScreenHandlers.CARGO_CRATE, syncId);
        this.inventory = inventory;
        this.context = context;
        inventory.onOpen(playerInventory.player);
        this.propertyDelegate = delegate;

        this.addSlot(new CargoCrateSlot(this.inventory, 0, 20, 20));
        this.addSlot(new CargoCrateSlot(this.inventory, 1, 50, 20));
        this.addSlot(new CargoCrateSlot(this.inventory, 2, 80, 20));

        // player inv
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public float getNormalizedProgress() {
        if (getMaxProgress() == 0) return 0;
        return MathHelper.clamp(this.getProgress() / this.getMaxProgress(), 0, 1);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public boolean allowsExtract(int extractAmount) {
        if (extractAmount == 1) {
            return this.propertyDelegate.get(2) == 1;
        }
        if (extractAmount == 9) {
            return this.propertyDelegate.get(3) == 1;
        }
        if (extractAmount == 27) {
            return this.propertyDelegate.get(4) == 1;
        }
        if (extractAmount == -1) {
            return this.propertyDelegate.get(5) == 1;
        }
        return false;
    }

    public Text getStoredMaterial() {
        Text material = this.inventory.getMaterial();
        return material == null ? Text.translatable("container.nemuelch.cargo_crate.empty") : material;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                    return ItemStack.EMPTY;
                }

                if (originalStack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }
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
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!(player instanceof ServerPlayerEntity)) return true;
        switch (id) {
            case 0 -> NeMuelch.LOGGER.info("Extract 1");
            case 1 -> NeMuelch.LOGGER.info("Extract 9");
            case 2 -> NeMuelch.LOGGER.info("Extract 27");
            case 3 -> NeMuelch.LOGGER.info("Extract All");
        }
        return super.onButtonClick(player, id);
    }
}
