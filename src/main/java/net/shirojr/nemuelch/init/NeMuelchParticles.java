package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public interface NeMuelchParticles {
    DefaultParticleType ROTTEN_MEAT_AIR = register("rotten_meat_air", false);
    DefaultParticleType MIASMA = register("miasma", false);


    @SuppressWarnings("SameParameterValue")
    private static DefaultParticleType register(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, NeMuelch.getId(name), FabricParticleTypes.simple(alwaysShow));
    }

    static void initialize() {
        // static initialisation
    }
}
