package net.shirojr.nemuelch.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class CargoCrateInventory implements Inventory {
    private final int size;
    private final DefaultedList<ItemStack> stacks;
    private final Runnable markedDirty;

    public CargoCrateInventory(int size, Runnable markedDirty) {
        this.size = size;
        this.stacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.markedDirty = markedDirty;
    }

    public DefaultedList<ItemStack> getStacks() {
        return stacks;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removedStack = Inventories.splitStack(stacks, slot, amount);
        markDirty();
        return removedStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removedStack = Inventories.removeStack(stacks, slot);
        markDirty();
        return removedStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (isValid(slot, stack)) {
            this.stacks.set(slot, stack);
            markDirty();
        }
    }

    public ItemStack insertStack(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        for (int i = 0; i < this.size(); i++) {
            ItemStack entryStack = this.stacks.get(i);
            if (entryStack.isEmpty() || !ItemStack.canCombine(entryStack, stack)) continue;
            int space = entryStack.getMaxCount() - entryStack.getCount();
            if (space <= 0) continue;
            int movableAmount = Math.min(stack.getCount(), space);
            entryStack.increment(movableAmount);
            stack.decrement(movableAmount);
            if (stack.isEmpty()) {
                break;
            }
        }
        if (!stack.isEmpty()) {
            for (int i = 0; i < this.size(); i++) {
                ItemStack entryStack = this.stacks.get(i);
                if (!entryStack.isEmpty()) continue;
                int movableAmount = stack.getCount();
                this.stacks.set(i, stack.copyWithCount(movableAmount));
                stack.decrement(movableAmount);
                if (stack.isEmpty()) break;
            }
        }
        markDirty();
        return stack;
    }

    @Nullable
    public ItemStack extractStack() {
        for (int i = this.stacks.size() - 1; i >= 0; i--) {
            ItemStack entryStack = this.stacks.get(i);
            if (entryStack.isEmpty()) continue;
            return removeStack(i);
        }
        return null;
    }

    @Override
    public void markDirty() {
        this.markedDirty.run();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        Collections.fill(this.stacks, ItemStack.EMPTY);
        this.markDirty();
    }
}
