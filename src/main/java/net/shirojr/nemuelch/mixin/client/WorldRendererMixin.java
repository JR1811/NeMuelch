package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Debug(export = true)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V",
                    ordinal = 0
            )
    )
    private void setSunTexture(int texture, Identifier id, Operation<Void> original) {
        if (client == null || client.world == null) {
            original.call(texture, id);
            return;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            original.call(texture, id);
            return;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        occasionEntry.getType().getSunSprite(client.world, occasionEntry).ifPresentOrElse(
                newIdentifier -> original.call(0, newIdentifier),
                () -> original.call(texture, id)
        );
    }

    @WrapOperation(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V",
                    ordinal = 1
            )
    )
    private void setMoonTexture(int texture, Identifier id, Operation<Void> original) {
        if (client == null || client.world == null) {
            original.call(texture, id);
            return;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            original.call(texture, id);
            return;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        occasionEntry.getType().getMoonSprite(client.world, occasionEntry).ifPresentOrElse(
                newIdentifier -> original.call(0, newIdentifier),
                () -> original.call(texture, id)
        );
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "CONSTANT", args = "floatValue=30.0")
    )
    private void setSunColor(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (client == null || client.world == null) {
            return;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            return;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        occasionEntry.getType().getSunColor(client.world, occasionEntry).ifPresent(vector4f -> RenderSystem.setShaderColor(vector4f.x, vector4f.y, vector4f.z, vector4f.w));
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "CONSTANT", args = "floatValue=20.0")
    )
    private void resetColorAfterSun(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;getMoonPhase()I"
            )
    )
    private void setMoonColor(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (client == null || client.world == null) {
            return;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            return;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        occasionEntry.getType().getMoonColor(client.world, occasionEntry).ifPresent(vector4f -> RenderSystem.setShaderColor(vector4f.x, vector4f.y, vector4f.z, vector4f.w));
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;method_23787(F)F")
    )
    private void resetColorAfterMoon(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @ModifyExpressionValue(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "CONSTANT", args = "floatValue=30.0")
    )
    private float adjustSunSize(float original) {

        return original;
    }

    @ModifyConstant(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            constant = @Constant(floatValue = OccasionType.ORIGINAL_SUN_SIZE)
    )
    private float setSunSize(float original) {
        if (client == null || client.world == null) {
            return original;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            return original;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        return occasionEntry.getType().getSunSize(client.world, occasionEntry).orElse(original);
    }

    @ModifyConstant(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            constant = @Constant(floatValue = OccasionType.ORIGINAL_MOON_SIZE)
    )
    private float setMoonSize(float original) {
        if (client == null || client.world == null) {
            return original;
        }
        if (getFirstActiveOccasion(client).isEmpty()) {
            return original;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion(client).get();
        return occasionEntry.getType().getMoonSize(client.world, occasionEntry).orElse(original);
    }

    @Unique
    private Optional<OccasionEntry> getFirstActiveOccasion(MinecraftClient client) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(client.world);
        List<OccasionEntry> occasions = component.getUnsyncedActiveOccasions();
        if (occasions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(occasions.get(0));
    }
}
