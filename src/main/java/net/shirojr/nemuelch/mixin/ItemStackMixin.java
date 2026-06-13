package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.nemuelch.effect.custom.AcidBurnStatusEffect;
import net.shirojr.nemuelch.init.NeMuelchEnchantments;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.item.custom.supportItem.SoapItem;
import net.shirojr.nemuelch.item.util.ItemCallbacks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void checkNbtForGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ItemStack.DISPLAY_KEY)) return;
        NbtCompound displayNbt = nbt.getCompound(ItemStack.DISPLAY_KEY);
        if (!displayNbt.contains("glint")) return;
        cir.setReturnValue(displayNbt.getBoolean("glint"));
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", ordinal = 1))
    private void addCustomTooltips(@Nullable PlayerEntity player, TooltipContext context,
                                   CallbackInfoReturnable<List<Text>> cir, @Local(ordinal = 0) List<Text> list) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!SoapItem.hasCoating(stack)) return;
        if (SoapItem.hasInfiniteCoating(stack)) {
            list.add(Text.translatable("item.nemuelch.soap_coating_infinite"));
        } else {
            list.add(Text.translatable("item.nemuelch.soap_coating", SoapItem.getCoatingCharges(stack)));
        }
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T extends LivingEntity> void callBeforeBrokenHandler(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.getItem() instanceof ItemCallbacks damageHandler) {
            damageHandler.nemuelch$onBroken(entity, itemStack);
        }
    }

    @Inject(method = "decrement", at = @At("HEAD"))
    private void callDecrementedHandler(int amount, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.getItem() instanceof ItemCallbacks handler) {
            handler.nemuelch$onDecremented(itemStack, amount);
        }
    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack clearAcid(Item instance, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {
        if (AcidBurnStatusEffect.CLEARS_ACID_ON_CONSUMPTION.test(stack)) {
            user.removeStatusEffect(NeMuelchStatusEffects.ACID_BURN);
        }
        return original.call(instance, stack, world, user);
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V"))
    private void disableEnchantmentTooltipsForEnchant(List<Text> tooltip, NbtList enchantments, Operation<Void> original, @Local(argsOnly = true) @Nullable PlayerEntity player) {
        if (player == null || player.isCreative() || player.isSpectator()) {
            original.call(tooltip, enchantments);
            return;
        }
        int veilingLevel = EnchantmentHelper.getLevel(NeMuelchEnchantments.CURSE_OF_VEILING, (ItemStack) (Object) this);
        if (veilingLevel <= 0) {
            original.call(tooltip, enchantments);
        }
    }
}
