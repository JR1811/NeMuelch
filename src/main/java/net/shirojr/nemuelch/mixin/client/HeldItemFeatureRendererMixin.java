package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {
    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean disableItemRendering(ItemStack instance, Operation<Boolean> original,
                                         @Local(argsOnly = true) LivingEntity entity,
                                         @Local(argsOnly = true) ModelTransformationMode transformationMode) {
        if (instance.getItem() instanceof DropPotBlockItem && entity.isFallFlying()) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return original.call(instance);
        if (client.player.hasPermissionLevel(2)) {
            return original.call(instance);
        }
        if (ThirdPersonInvisible.isInvisible(instance)) {
            if (transformationMode.equals(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) || transformationMode.equals(ModelTransformationMode.THIRD_PERSON_LEFT_HAND)) {
                return true;
            }
        }
        return original.call(instance);
    }
}
