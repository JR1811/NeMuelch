package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.OptionalInt;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;getWaterFogColor()I"
            )
    )
    private static int adjustUnderWaterFogColorForOccasion(Biome instance, Operation<Integer> original, @Local(argsOnly = true) ClientWorld world) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            OptionalInt fogWaterColor = entry.getType().getFogWaterColor(world);
            if (fogWaterColor.isPresent()) {
                return fogWaterColor.getAsInt();
            }
        }
        return original.call(instance);
    }
}
