package net.shirojr.nemuelch.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    @Inject(method = "getMovementMultiplier", at = @At("HEAD"), cancellable = true)
    private static void nemuelch$stuckEffectMovementMultiplier(boolean positive, boolean negative, CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        if (clientPlayer != null && !clientPlayer.isSpectator() &&
                clientPlayer.hasStatusEffect(NeMuelchEffects.STUCK)) {

            cir.setReturnValue(0.0f);
        }
    }

    //FIXME: is only client sided
}
