package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;

@Mixin(MatrixStack.class)
public class MatrixStackDebugMixin {
    @Unique
    private final ArrayDeque<Throwable> pushSites = new ArrayDeque<>();

    @Inject(method = "push", at = @At("HEAD"))
    private void debugPush(CallbackInfo ci) {
        pushSites.push(new Throwable("MatrixStack.push() called here"));
    }

    @Inject(method = "pop", at = @At("HEAD"))
    private void onPop(CallbackInfo ci) {
        if (!pushSites.isEmpty()) pushSites.pop();
    }

    @Inject(method = "isEmpty", at = @At("RETURN"))
    private void onIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && !pushSites.isEmpty()) {
            LogManager.getLogger("MatrixStackDebug").error(
                    "=== LEAKED PUSH SITE ===", pushSites.peek()
            );
        }
    }
}
