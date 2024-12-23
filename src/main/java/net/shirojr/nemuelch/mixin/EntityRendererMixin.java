package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;
import net.shirojr.nemuelch.NeMuelchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"))
    private int renderObfuscatedLabel(TextRenderer instance, Text text, float x, float y, int color, boolean shadow,
                                      Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough,
                                      int backgroundColor, int light, Operation<Integer> original, @Local(argsOnly = true) Entity entity) {
        Optional<Boolean> isObfuscated = Optional.ofNullable(NeMuelchClient.OBFUSCATED_CACHE.get(entity.getUuid()));
        MutableText newText = text.copy();
        if (isObfuscated.orElse(false)) {
            newText.setStyle(text.getStyle().withFormatting(Formatting.OBFUSCATED));
        }
        return original.call(instance, newText, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light);
    }
}
