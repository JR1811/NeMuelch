package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "hasReducedDebugInfo", at = @At("HEAD"), cancellable = true)
    private void showFullDebugInfoToOperators(CallbackInfoReturnable<Boolean> cir) {
        if (!NeMuelchConfigInit.CONFIG.disableReducedDebugInfoForOperators) return;
        if (player != null && player.hasPermissionLevel(2)) {
            cir.setReturnValue(false);
        }
    }
}
