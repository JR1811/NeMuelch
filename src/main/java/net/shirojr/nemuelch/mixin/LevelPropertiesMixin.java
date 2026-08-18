package net.shirojr.nemuelch.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.LevelProperties;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin {
    @Inject(method = "getLifecycle", at = @At("RETURN"), cancellable = true)
    private void preventExperimentalWorldScreen(CallbackInfoReturnable<Lifecycle> cir) {
        if (NeMuelchConfigInit.CONFIG.hideExperimentalWorldScreen) {
            if (cir.getReturnValue() == Lifecycle.experimental()) {
                cir.setReturnValue(Lifecycle.stable());
            }
        }
    }
}
