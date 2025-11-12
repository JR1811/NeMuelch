package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @ModifyExpressionValue(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean disableRendering(boolean original, @Local ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return original;
        if (client.player.hasPermissionLevel(2)) return original;
        return ThirdPersonInvisible.isInvisible(stack);
    }
}
