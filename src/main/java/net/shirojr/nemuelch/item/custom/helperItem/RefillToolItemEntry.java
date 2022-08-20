package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import javax.annotation.Nullable;
import java.util.List;

public class RefillToolItemEntry {

    private ItemStack itemStack;
    private int count = 1;

    //region constructor
    /**
     * Constructor instances the object with an amount of 0 items
     * @param itemStack Item for saving
     */
    public RefillToolItemEntry(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Constructor instances the object with a given amount
     * @param itemStack Item for saving
     * @param count Amount of how many of those items are saved when calling the Constructor
     */
    public RefillToolItemEntry(ItemStack itemStack, int count) {
        this.itemStack = itemStack;
        this.count = count;
    }
    //endregion


    //region getter & setter
    public String getItemName() {
        return itemStack.getName().toString();
    }

    public String getItemTranslationKey() {
        return itemStack.getTranslationKey().toString();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getCount() {
        return count;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setCount(int count) {
        this.count = count;
    }
    //endregion


    /**
     * Add a specific value on top of the already existing amount of items
     * @param number value that will be added
     */
    public void addCount(int number) {
        count += number;
    }

    /**
     * Check if {@code this.itemStack} exists in given list
     * @param inputList List of items that needs to be checked
     * @return True if saved itemStack exists in the given list
     */
    public boolean doesExistInList(List<ItemStack> inputList) {

        for (ItemStack stack : inputList) {
            if (stack.isItemEqual(itemStack)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasNbtData() {
        return itemStack.hasNbt();
    }
}
