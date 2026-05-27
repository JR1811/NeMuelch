package net.shirojr.nemuelch.fluid;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class CrimsonPhaseWaterFluidWrapper implements FluidRenderHandler {
    private final FluidRenderHandler parent;

    public CrimsonPhaseWaterFluidWrapper(FluidRenderHandler parent) {
        this.parent = parent;
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return parent.getFluidSprites(view, pos, state);
    }

    @Override
    public int getFluidColor(@Nullable BlockRenderView world, @Nullable BlockPos pos, FluidState state) {
        if (!(world instanceof ClientWorld clientWorld)) return parent.getFluidColor(world, pos, state);
        OccasionsWorldComponent component = OccasionsWorldComponent.get(clientWorld);
        for (OccasionEntry occasionEntry : component.getUnsyncedActiveOccasions()) {
            OptionalInt color = occasionEntry.getType().getGlobalWaterColor(world, pos);
            if (color.isPresent()) {
                return color.getAsInt();
            }
        }
        return parent.getFluidColor(world, pos, state);
    }
}
