package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Debug(export = true)
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;", ordinal = 0))
    private Vector3f adjustLerpedColor(Vector3f instance, Vector3fc other, float t, Operation<Vector3f> original) {
        Vector3f result = original.call(instance, other, t);
        ClientWorld world = client.world;
        if (client.world != null) {
            OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
            for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
                Optional<Vector3f> skyLightModifier = entry.getType().getSkyLightColorModifier();
                if (skyLightModifier.isPresent()) {
                    Vector3f modifier = skyLightModifier.get();
                    result.set(result.x * modifier.x, result.y * modifier.y, result.z * modifier.z);
                    break;
                }
            }
        }
        return result;
    }
}
