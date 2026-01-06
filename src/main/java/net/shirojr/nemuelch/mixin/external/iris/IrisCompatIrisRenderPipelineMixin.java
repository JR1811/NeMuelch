package net.shirojr.nemuelch.mixin.external.iris;

import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisRenderingPipeline.class)
public class IrisCompatIrisRenderPipelineMixin {
    @Inject(method = "finalizeLevelRendering", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelRenderingForInternalShaders(CallbackInfo ci) {
        if (NeMuelchShaderManager.getActiveShadersCount() > 0) {
            ci.cancel();
        }
    }
}
