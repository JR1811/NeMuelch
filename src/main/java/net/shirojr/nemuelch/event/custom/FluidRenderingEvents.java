package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.shirojr.nemuelch.fluid.CrimsonPhaseWaterFluidWrapper;

public class FluidRenderingEvents {
    public static void initialize() {
        FluidRenderHandler water = FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER);
        FluidRenderHandler flowingWater = FluidRenderHandlerRegistry.INSTANCE.get(Fluids.FLOWING_WATER);

        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, new CrimsonPhaseWaterFluidWrapper(water));
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_WATER, new CrimsonPhaseWaterFluidWrapper(flowingWater));
    }
}
