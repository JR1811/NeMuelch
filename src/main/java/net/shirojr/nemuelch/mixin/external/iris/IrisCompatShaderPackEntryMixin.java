package net.shirojr.nemuelch.mixin.external.iris;

import net.irisshaders.iris.gui.element.ShaderPackSelectionList;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderPackSelectionList.ShaderPackEntry.class)
public class IrisCompatShaderPackEntryMixin {
    @Inject(method = "doThing", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelTheThingIfBlocked(CallbackInfoReturnable<Boolean> cir) {
        if (IrisCompat.getShaderToggleLocker().neMuelch$isLocked()) {
            IrisCompat.onInteractWithLocked();
            cir.setReturnValue(false);
        }
    }
}
