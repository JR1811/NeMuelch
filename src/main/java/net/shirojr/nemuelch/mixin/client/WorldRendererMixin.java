package net.shirojr.nemuelch.mixin.client;

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
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @WrapOperation(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V",
                    ordinal = 1
            )
    )
    private void setMoonTexture(int texture, Identifier id, Operation<Void> original) {
        if (getFirstActiveOccasion().isEmpty()) {
            original.call(texture, id);
            return;
        }

        OccasionEntry occasionEntry = getFirstActiveOccasion().get();
        occasionEntry.getType().getMoonSprite().ifPresentOrElse(
                newIdentifier -> original.call(0, newIdentifier),   //FIXME: no moon texture displayed?
                () -> original.call(texture, id)
        );
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;getMoonPhase()I"
            )
    )
    private void setMoonColor(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (getFirstActiveOccasion().isEmpty()) {
            return;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion().get();
        occasionEntry.getType().getMoonColor().ifPresent(vector4f -> RenderSystem.setShaderColor(vector4f.x, vector4f.y, vector4f.z, vector4f.w));
    }

    @ModifyConstant(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            constant = @Constant(floatValue = 20.0f)
    )
    private float setMoonSize(float original) {
        if (getFirstActiveOccasion().isEmpty()) {
            return original;
        }
        OccasionEntry occasionEntry = getFirstActiveOccasion().get();
        return occasionEntry.getType().getMoonSize().orElse(original);
    }

    @Unique
    private Optional<OccasionEntry> getFirstActiveOccasion() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return Optional.empty();
        }
        OccasionsWorldComponent component = OccasionsWorldComponent.get(client.world);
        List<OccasionEntry> occasions = component.getUnsyncedActiveOccasions();
        if (occasions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(occasions.get(0));
    }
}
