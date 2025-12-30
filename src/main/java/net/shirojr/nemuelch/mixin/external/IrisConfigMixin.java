package net.shirojr.nemuelch.mixin.external;

import net.irisshaders.iris.config.IrisConfig;
import net.shirojr.nemuelch.util.duck.IrisConfigShaderToggleLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = IrisConfig.class)
public class IrisConfigMixin implements IrisConfigShaderToggleLock {
    @Unique
    private boolean shaderToggleLocked = false;

    /*@Inject(method = "setShadersEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private void lockShaderToggle(boolean enabled, CallbackInfo ci) {
        if (neMuelch$isLocked()) {
            ci.cancel();
        }
    }*/

    @Override
    public boolean neMuelch$isLocked() {
        return shaderToggleLocked;
    }

    @Override
    public void neMuelch$setLocked(boolean locked) {
        shaderToggleLocked = locked;
    }
}
