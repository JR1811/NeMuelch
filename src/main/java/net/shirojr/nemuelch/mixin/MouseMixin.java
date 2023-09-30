package net.shirojr.nemuelch.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "updateMouse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"),
            cancellable = true)
    private void nemuelch$stuckEffectMovementMultiplier(CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        if (clientPlayer == null || clientPlayer.isSpectator()) return;
        for (StatusEffect effect : NeMuelchEffects.STUCK_EFFECTS) {
            if (!clientPlayer.hasStatusEffect(effect)) continue;
            ci.cancel();
        }
    }
}
