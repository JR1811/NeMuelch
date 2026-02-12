package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.particle.MiasmaParticle;
import net.shirojr.nemuelch.particle.RottenMeatAirParticle;

public class NeMuelchParticleFactories {
    static {
        register(NeMuelchParticles.ROTTEN_MEAT_AIR, RottenMeatAirParticle.Factory::new);
        register(NeMuelchParticles.MIASMA, provider -> new MiasmaParticle.Factory());
        register(NeMuelchParticles.MIASMA_RED, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.RED));
        register(NeMuelchParticles.MIASMA_BLUE, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.BLUE));
        register(NeMuelchParticles.MIASMA_GREEN, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.GREEN));
        register(NeMuelchParticles.MIASMA_BROWN, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.BROWN));
    }


    @SuppressWarnings("SameParameterValue")
    private static <T extends ParticleEffect> void register(ParticleType<T> type, ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(type, factory);
    }

    public static void initialize() {
        // static initialisation
    }
}
