package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.supportItem.WateringCanItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @WrapOperation(method = "useOnGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private static void avoidWateringCanStackDecrement(ItemStack instance, int amount, Operation<Void> original) {
        if (!(instance.getItem() instanceof WateringCanItem)) {
            original.call(instance, amount);
        }
    }

    @Inject(method = "useOnFertilizable", at = @At(value = "HEAD"), cancellable = true)
    private static void limitBoneMealUsage(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        if (!NeMuelchConfigInit.CONFIG.enableFertilizableBlockWhitelistFeature) return;
        if (!state.isIn(NeMuelchTags.Blocks.FERTILIZABLE_WHITELIST)) {
            cir.setReturnValue(false);
        }
    }
}
