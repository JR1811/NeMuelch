package net.shirojr.nemuelch.init;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.particle.data.SwipeParticleEffect;

import java.util.function.Function;

public interface NeMuelchParticleTypes {
    DefaultParticleType ROTTEN_MEAT_AIR = registerSimple("rotten_meat_air", false);
    DefaultParticleType MIASMA = registerSimple("miasma", true);
    DefaultParticleType MIASMA_RED = registerSimple("miasma_red", true);
    DefaultParticleType MIASMA_BLUE = registerSimple("miasma_blue", true);
    DefaultParticleType MIASMA_GREEN = registerSimple("miasma_green", true);
    DefaultParticleType MIASMA_BROWN = registerSimple("miasma_brown", true);
    ParticleType<SwipeParticleEffect> SWIPE_UP = registerComplex("swipe_up", false,
            SwipeParticleEffect.FACTORY, SwipeParticleEffect::getCodec);
    ParticleType<SwipeParticleEffect> SWIPE_DOWN = registerComplex("swipe_down", false,
            SwipeParticleEffect.FACTORY, SwipeParticleEffect::getCodec);


    private static DefaultParticleType registerSimple(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, NeMuelch.getId(name), FabricParticleTypes.simple(alwaysShow));
    }

    @SuppressWarnings({"SameParameterValue", "deprecation"})
    private static <T extends ParticleEffect, S extends ParticleEffect.Factory<T>> ParticleType<T> registerComplex(
            String name, boolean alwaysShow, S entry, Function<ParticleType<T>, Codec<T>> codecGetter) {
        return Registry.register(Registries.PARTICLE_TYPE, NeMuelch.getId(name), new ParticleType<T>(alwaysShow, entry) {
            @Override
            public Codec<T> getCodec() {
                return codecGetter.apply(this);
            }
        });
    }

    static void initialize() {
        // static initialisation
    }
}
