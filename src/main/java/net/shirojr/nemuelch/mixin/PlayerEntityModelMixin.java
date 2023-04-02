package net.shirojr.nemuelch.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin extends BipedEntityModel implements ModelWithArms, ModelWithHead {
    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "getBodyParts", at = @At("TAIL"), cancellable = true)
    private void getBodyParts(CallbackInfoReturnable<Iterable<ModelPart>> cir) {
        //cir.setReturnValue(ImmutableList.of(((BipedEntityModel<?>)(Object)this).body));
        //first bad one 0938010ba508f0ed175ac870a15216d38c9e1584
    }
}
