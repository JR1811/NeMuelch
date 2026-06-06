package net.shirojr.nemuelch.event.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.event.custom.AcidCallbacks;
import net.shirojr.nemuelch.init.NeMuelchTags;

public class AcidEvents implements AcidCallbacks.DirectContact, AcidCallbacks.AtmosphereContact {
    @Override
    public boolean isContactProtected(Entity entity) {
        return entity.getType().isIn(NeMuelchTags.EntityTypes.ACID_IMMUNE);
    }

    @Override
    public boolean isAtmosphereProtected(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        int protectionCount = 0;
        for (ItemStack stack : livingEntity.getArmorItems()) {
            if (stack.isIn(NeMuelchTags.Items.ACID_PROTECTING_ARMOR)) {
                return true;
            }
            if (stack.isIn(NeMuelchTags.Items.ACID_PROTECTING_FULL_GEARED_ARMOR)) {
                protectionCount += 1;
            }
        }
        return protectionCount >= 4;
    }
}
