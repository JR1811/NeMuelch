package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class SmokingPipeItem extends Item {
    public static final String LIT_NBT_KEY = "Lit";
    public static final String FILLING_NBT_KEY = "Filling";

    public SmokingPipeItem(Settings settings) {
        super(settings);
    }

    public static boolean isLit(ItemStack stack) {
        return stack.getNbt() != null && stack.getNbt().contains(LIT_NBT_KEY) && stack.getNbt().getBoolean(LIT_NBT_KEY);
    }

    public static void setLit(ItemStack stack, boolean lit) {
        stack.getOrCreateNbt().putBoolean(LIT_NBT_KEY, lit);
    }

    public static List<StatusEffectInstance> getFilling(ItemStack stack) {
        List<StatusEffectInstance> instances = new ArrayList<>();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(FILLING_NBT_KEY)) return instances;
        NbtList fillingNbtList = nbt.getList(FILLING_NBT_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < fillingNbtList.size(); i++) {
            NbtCompound fillingNbt = fillingNbtList.getCompound(i);
            instances.add(StatusEffectInstance.fromNbt(fillingNbt));
        }
        return instances;
    }

    public static void setFilling(ItemStack stack, List<StatusEffectInstance> instances) {
        NbtList fillingNbtList = new NbtList();
        for (StatusEffectInstance instance : instances) {
            fillingNbtList.add(instance.writeNbt(new NbtCompound()));
        }
        stack.getOrCreateNbt().put(FILLING_NBT_KEY, fillingNbtList);
    }
}
