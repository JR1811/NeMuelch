package net.shirojr.nemuelch.mixin.external.iris;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.irisshaders.iris.gui.element.ShaderPackSelectionList;
import net.shirojr.nemuelch.compat.iris.IrisCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderPackSelectionList.TopButtonRowEntry.class)
public class IrisCompatTopButtonRowEntryMixin {
    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/irisshaders/iris/gui/element/ShaderPackSelectionList$TopButtonRowEntry;allowEnableShadersButton:Z",
                    opcode = Opcodes.GETFIELD
            ),
            remap = false
    )
    private boolean handleShaderLock(boolean original) {
        if (IrisCompat.getShaderToggleLocker().neMuelch$isLocked()) {
            IrisCompat.onInteractWithLocked();
            return false;
        }
        return original;
    }
}
