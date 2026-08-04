package net.shirojr.nemuelch.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class HandInventory implements Inventory {
    @Nullable PlayerEntity player = null;
    private final DefaultedList<ItemStack> stacks;

    public HandInventory(ItemStack mainHandStack, ItemStack offHandStack) {
        this.stacks = DefaultedList.ofSize(2, ItemStack.EMPTY);
        this.stacks.set(0, mainHandStack);
        this.stacks.set(1, offHandStack);
    }

    public HandInventory(PlayerEntity player) {
        this(player.getMainHandStack(), player.getOffHandStack());
        this.player = player;
    }

    public void applyToPlayerIfPresent() {
        if (player == null) return;
        player.setStackInHand(Hand.MAIN_HAND, getMainHandStack());
        player.setStackInHand(Hand.OFF_HAND, getOffHandStack());
        player.getInventory().markDirty();
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (ItemStack stack : stacks) {
            if (predicate.test(stack)) return true;
        }
        return false;
    }

    public ItemStack getMainHandStack() {
        return stacks.get(0);
    }

    public ItemStack getOffHandStack() {
        return stacks.get(1);
    }

    @Nullable
    public ItemStack getOther(ItemStack stack) {
        if (stack.equals(getStack(0))) return getStack(1);
        if (stack.equals(getStack(1))) return getStack(0);
        return null;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return getMainHandStack().isEmpty() && getOffHandStack().isEmpty();
    }

    public boolean isFull() {
        return !getMainHandStack().isEmpty() && !getOffHandStack().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Nullable
    public ItemStack getStack(Predicate<ItemStack> predicate) {
        if (predicate.test(getStack(0))) return getStack(0);
        if (predicate.test(getStack(1))) return getStack(1);
        return null;
    }

    public List<ItemStack> getStacks() {
        return Collections.unmodifiableList(this.stacks);
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
        this.stacks.set(slot, stack);
        markDirty();
    }

    public void setMainHandStack(ItemStack stack) {
        setStack(0, stack);
    }

    public void setOffHandStack(ItemStack stack) {
        setStack(1, stack);
    }

    @Override
    public void markDirty() {
        this.applyToPlayerIfPresent();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return isFull();
    }

    public void decrement(int amount) {
        this.getMainHandStack().decrement(amount);
        this.getOffHandStack().decrement(amount);
        markDirty();
    }

    public void decrement() {
        this.decrement(1);
    }

    @Override
    public void clear() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        markDirty();
    }
}
