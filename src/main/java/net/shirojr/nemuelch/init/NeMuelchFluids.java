package net.shirojr.nemuelch.init;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.fluid.custom.NeMuelchHoneyFluid;
import net.shirojr.nemuelch.fluid.custom.NeMuelchSlimeFluid;

public class NeMuelchFluids {
    public static final FlowableFluid SLIME_STILL = register("slime_still", new NeMuelchSlimeFluid.Still());
    public static final FlowableFluid SLIME_FLOWING = register("slime_flowing", new NeMuelchSlimeFluid.Flowing());

    public static final FlowableFluid HONEY_STILL = register("honey_still", new NeMuelchHoneyFluid.Still());
    public static final FlowableFluid HONEY_FLOWING = register("honey_flowing", new NeMuelchHoneyFluid.Flowing());

    private static FlowableFluid register(String name, FlowableFluid flowableFluid) {
        return Registry.register(Registries.FLUID, new Identifier(NeMuelch.MOD_ID, name), flowableFluid);
    }

    public static void initialize() {
        // static initialisation
    }
}
