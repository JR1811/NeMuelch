package net.shirojr.nemuelch.mixin.external.iris;

import net.irisshaders.iris.gui.element.ShaderPackSelectionList;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(ShaderPackSelectionList.TopButtonRowEntry.class)
public class IrisCompatTopButtonRowEntryMixin {
    @Inject(
            method = "mouseClicked",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false
    )
    private void handleShaderLock(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (IrisCompat.getShaderToggleLocker().neMuelch$isLocked()) {
            IrisCompat.onInteractWithLocked();
            cir.setReturnValue(false);
        }
    }
}
