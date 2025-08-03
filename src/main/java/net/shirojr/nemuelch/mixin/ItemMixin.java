package net.shirojr.nemuelch.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void useAddition(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack.getItem() instanceof ShearsItem && user.getWorld() instanceof ServerWorld serverWorld) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                if (serverPlayer.isSneaking()) {
                    float pitch = MathHelper.lerp(serverWorld.getRandom().nextFloat(), 0.7f, 1.3f);
                    serverWorld.playSound(null, user.getBlockPos(), NeMuelchSounds.SHEARS_SNAP, SoundCategory.PLAYERS, 2f, pitch);
                    if (!serverPlayer.isCreative()) {
                        stack.damage(1, serverWorld.getRandom(), serverPlayer);
                    }
                    cir.setReturnValue(TypedActionResult.success(stack));
                }
            }
        }
    }

    @Inject(method = "appendTooltip", at = @At("TAIL"))
    private void appendAdditionalTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (stack.getItem() instanceof ShearsItem) {
            tooltip.add(Text.translatable("item.nemuelch.shear_snap"));
        }
    }
}
