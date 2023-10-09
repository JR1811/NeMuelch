package net.shirojr.nemuelch.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SleepManager.class)
public class SleepManagerMixin {
    @Inject(method = "canResetTime", at = @At("HEAD"), cancellable = true)
    private void nemuelch$sleepEventTime(int percentage, List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> cir) {
        if (SleepEventHelper.isSleepEventTime()) {
            cir.setReturnValue(false);
        }
    }
}
