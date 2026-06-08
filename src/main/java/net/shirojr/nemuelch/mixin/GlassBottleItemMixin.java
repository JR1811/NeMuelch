package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.implementation.AcidEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin {
    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/GlassBottleItem;fill(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    ordinal = 1
            )
    )
    private ItemStack fillWithAcid(GlassBottleItem instance, ItemStack stack, PlayerEntity player, ItemStack outputStack,
                                   Operation<ItemStack> original, @Local BlockPos pos) {
        if (!AcidEntityComponent.isInAtmosphericAcid(player.getWorld(), pos)) {
            return original.call(instance, stack, player, outputStack);
        }
        return original.call(instance, stack, player, PotionUtil.setPotion(Items.POTION.getDefaultStack(), NeMuelchPotions.ACID_BURN));
    }
}
