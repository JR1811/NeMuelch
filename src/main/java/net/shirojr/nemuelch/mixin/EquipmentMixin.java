package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Equipment.class)
public interface EquipmentMixin {
    @Inject(method = "equipAndSwap", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasBindingCurse(Lnet/minecraft/item/ItemStack;)Z"), cancellable = true)
    private void blockEquipmentSwapForEnchantments(Item item, World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (EnchantmentHelper.getLevel(NeMuelchEnchantments.CURSE_OF_THE_BARE, stack) > 0) {
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}
