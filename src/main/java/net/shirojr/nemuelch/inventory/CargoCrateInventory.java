package net.shirojr.nemuelch.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public class CargoCrateInventory implements Inventory {
    public static final BiPredicate<ItemStack, ItemStack> MATCH = (stackA, stackB) -> stackA.getItem().equals(stackB.getItem());

    private final int size;
    private final DefaultedList<ItemStack> stacks;
    private final Runnable markedDirty;

    public CargoCrateInventory(int size, Runnable markedDirty) {
        this.size = size;
        this.stacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.markedDirty = markedDirty;
    }

    public CargoCrateInventory(Runnable markedDirty, DefaultedList<ItemStack> initialStacks) {
        this(initialStacks.size(), markedDirty);
        this.replaceStacks(initialStacks);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canInsert(ItemStack stack) {
        if (this.isEmpty()) return true;
        for (ItemStack inventoryStack : this.stacks) {
            if (inventoryStack.isEmpty()) continue;
            if (!MATCH.test(stack, inventoryStack)) return false;
        }
        return true;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (!this.canInsert(stack)) {
            throw new IllegalStateException("Cargo crate can't hold this item");
        }
        if (isValid(slot, stack)) {
            this.stacks.set(slot, stack);
            markDirty();
        }
    }

    public void replaceStacks(DefaultedList<ItemStack> newStacks) {
        if (this.stacks.size() != newStacks.size()) {
            throw new IllegalArgumentException("Replacing DefaultedList needs to be of same size as Original DefaultedList");
        }
        for (int i = 0; i < newStacks.size(); i++) {
            ItemStack newStack = newStacks.get(i);
            this.stacks.set(i, newStack);
        }
    }

    /**
     *
     * @return left-over ItemStack
     */
    public ItemStack insertStack(ItemStack stack) {
        if (!this.canInsert(stack)) return stack;
        for (int i = 0; i < this.size(); i++) {
            ItemStack inventoryStack = this.stacks.get(i);
            if (inventoryStack.isEmpty() || !ItemStack.canCombine(inventoryStack, stack)) continue;
            int space = inventoryStack.getMaxCount() - inventoryStack.getCount();
            if (space <= 0) continue;
            int movableAmount = Math.min(stack.getCount(), space);
            inventoryStack.increment(movableAmount);
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

    public List<ItemStack> insertStacks(Collection<ItemStack> insertionStacks) {
        List<ItemStack> leftOverStacks = new ArrayList<>();
        for (ItemStack insertionStack : insertionStacks) {
            ItemStack leftOverStack = this.insertStack(insertionStack);
            if (leftOverStack.isEmpty()) continue;
            leftOverStacks.add(leftOverStack);
        }
        return leftOverStacks;
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

    public void readNbt(NbtCompound nbt) {
        this.clear();
        NbtCompound cargoCrateNbt = nbt.getCompound(NeMuelchNbtKeys.CARGO_CRATE_INVENTORY);
        if (cargoCrateNbt.isEmpty()) return;
        int size = cargoCrateNbt.getInt(NeMuelchNbtKeys.SIZE);
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
        NbtList inventoryNbt = cargoCrateNbt.getList(NeMuelchNbtKeys.INVENTORY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < inventoryNbt.size(); i++) {
            NbtCompound entryNbt = inventoryNbt.getCompound(i);
            int index = entryNbt.getInt(NeMuelchNbtKeys.INDEX);
            ItemStack stack = ItemStack.fromNbt(entryNbt);
            if (!stack.isEmpty()) {
                stacks.set(index, stack);
            }
        }
        this.replaceStacks(stacks);
    }

    public void writeNbt(NbtCompound nbt) {
        NbtCompound cargoCrateNbt = new NbtCompound();
        cargoCrateNbt.putInt(NeMuelchNbtKeys.SIZE, this.size());
        NbtList inventoryNbt = new NbtList();
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack stack = this.stacks.get(i);
            if (stack.isEmpty()) continue;
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putInt(NeMuelchNbtKeys.INDEX, i);
            stack.writeNbt(entryNbt);
            inventoryNbt.add(entryNbt);
        }
        cargoCrateNbt.put(NeMuelchNbtKeys.INVENTORY, inventoryNbt);
        nbt.put(NeMuelchNbtKeys.CARGO_CRATE_INVENTORY, cargoCrateNbt);
    }
}
