package net.shirojr.nemuelch.fluid;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.fluid.custom.NeMuelchHoneyFluid;
import net.shirojr.nemuelch.fluid.custom.NeMuelchSlimeFluid;

public abstract class NeMuelchFluid {
    public static final FlowableFluid SLIME_STILL = register("slime_still", new NeMuelchSlimeFluid.Still());
    public static final FlowableFluid SLIME_FLOWING = register("slime_flowing", new NeMuelchSlimeFluid.Flowing());

    public static final FlowableFluid HONEY_STILL = register("honey_still", new NeMuelchHoneyFluid.Still());
    public static final FlowableFluid HONEY_FLOWING = register("honey_flowing", new NeMuelchHoneyFluid.Flowing());

    private static FlowableFluid register(String name, FlowableFluid flowableFluid) {
        return Registry.register(Registry.FLUID, new Identifier(NeMuelch.MOD_ID, name), flowableFluid);
    }
}
