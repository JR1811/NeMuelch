package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.nbt.NbtCompound;

public class StoredItemInTool {
    private String translationKey;  // item name address in lang files
    private int count;
    private NbtCompound nbtData;


    /**
     * Access stored items with no nbt tags
     * @param translationKey item name address in lang files
     * @param count ammount of items stored
     */
    public StoredItemInTool(String translationKey, int count) {

        this.translationKey = translationKey;
        this.count = count;

        nbtData = null; // item has no nbt tags
    }

    /**
     * Access stored items with nbt tags
     * @param translationKey
     * @param count
     * @param nbtData
     */
    public StoredItemInTool(String translationKey, int count, NbtCompound nbtData) {
        this.translationKey = translationKey;
        this.count = count;

        this.nbtData = nbtData;
    }


    public String getTranslationKey(){
        return translationKey;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public NbtCompound getNbtData() {
        return nbtData;
    }
}


