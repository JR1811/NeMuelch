package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class EntityUtil {
    private EntityUtil() {

    }

    public static boolean hasAnyArmorEquipped(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    public static boolean hasAllArmorSlotsFilled(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }
}
