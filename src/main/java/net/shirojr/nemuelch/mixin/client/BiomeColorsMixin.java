package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Debug(export = true)
@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {
    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private static void tintWaterForOccasion(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!(world instanceof ClientWorld clientWorld)) return;
        OccasionsWorldComponent component = OccasionsWorldComponent.get(clientWorld);
        for (OccasionEntry occasionEntry : component.getUnsyncedActiveOccasions()) {
            OptionalInt color = occasionEntry.getType().getGlobalWaterColor(world, pos);
            if (color.isPresent()) {
                cir.setReturnValue(color.getAsInt());
                return;
            }
        }
    }
}
