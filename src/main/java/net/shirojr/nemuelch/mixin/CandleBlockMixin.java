package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.supportItem.BookWrapperItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CandleBlock.class)
public abstract class CandleBlockMixin extends AbstractCandleBlock implements Waterloggable {
    @Shadow @Final public static BooleanProperty LIT;

    private CandleBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean preventBookWrapperItemCandleInteraction(boolean original, @Local(argsOnly = true) PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof BookWrapperItem) return false;
        if (player.getOffHandStack().getItem() instanceof BookWrapperItem) return false;
        return original;
    }

    @Inject(method = "onUse", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$lightUpWithTorch(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        if (!NeMuelchConfigInit.CONFIG.campfireUtilities) return;
        if (!state.contains(LIT) || state.get(LIT)) return;
        if (player.getMainHandStack().isIn(NeMuelchTags.Items.CAMPFIRE_IGNITER)) {
            if (world instanceof ServerWorld serverWorld) {
                player.getStackInHand(hand).decrement(1);
                world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL);
                serverWorld.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 2f, 1f);
            }
            info.setReturnValue(ActionResult.success(world.isClient()));
        }
    }
}
