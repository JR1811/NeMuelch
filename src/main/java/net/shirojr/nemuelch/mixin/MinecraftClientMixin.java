package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.shirojr.nemuelch.init.ConfigInit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public ClientPlayerEntity player;

    @ModifyExpressionValue(
            method = "handleInputEvents",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;inventoryKey:Lnet/minecraft/client/option/KeyBinding;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 0)
    )
    private boolean nemuelch$handleInventoryScreenAccess(boolean original) {
        MinecraftClient client = (MinecraftClient) (Object)this;
        PlayerEntity clientPlayer = client.player;

        if (clientPlayer != null && clientPlayer.isFallFlying() && !clientPlayer.isCreative()
                && ConfigInit.CONFIG.blockPlayerInventoryWhenFlying) {
            if (client.options.inventoryKey.isPressed()) {
                clientPlayer.sendMessage(new TranslatableText("chat.nemuelch.inventoryblock.fallflying"), true);
            }
            return false;
        }
        return original;
    }
}
