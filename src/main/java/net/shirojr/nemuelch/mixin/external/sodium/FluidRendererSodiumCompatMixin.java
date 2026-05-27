package net.shirojr.nemuelch.mixin.external.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.OptionalInt;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererSodiumCompatMixin {

    @WrapOperation(
            method = "updateQuad",
            at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/color/ColorProvider;getColors(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/util/math/BlockPos;Ljava/lang/Object;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;[I)V"),
            remap = false
    )
    private <T> void adjustColorsForOccasion(ColorProvider<FluidState> instance, WorldSlice worldSlice, BlockPos pos,
                                             T t, ModelQuadView modelQuadView, int[] ints, Operation<Void> original) {
        original.call(instance, worldSlice, pos, t, modelQuadView, ints);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientWorld clientWorld = client.world;
        if (clientWorld == null || !(t instanceof FluidState fluidState) || !fluidState.isIn(FluidTags.WATER)) return;
        OccasionsWorldComponent component = OccasionsWorldComponent.get(clientWorld);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            OptionalInt color = entry.getType().getGlobalWaterColor(clientWorld, pos);
            if (color.isPresent()) {
                int rgb = color.getAsInt();
                int abgr = ColorABGR.pack(((rgb >> 16) & 0xFF) / 255.0f, ((rgb >> 8) & 0xFF) / 255.0f, (rgb & 0xFF) / 255.0f);
                Arrays.fill(ints, abgr);
            }
        }
    }
}
