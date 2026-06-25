package net.shirojr.nemuelch.mixin;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantmentTarget.class)
public enum EnchantmentTargetsMixin {
    NEMUELCH_SHIELD() {
        @Override
        public boolean isAcceptableItem(Item item) {
            return item instanceof ShieldItem || item.getDefaultStack().isIn(ConventionalItemTags.SHIELDS);
        }
    };

    @Shadow
    EnchantmentTargetsMixin() {
    }

    @Shadow
    public abstract boolean isAcceptableItem(Item item);
}
