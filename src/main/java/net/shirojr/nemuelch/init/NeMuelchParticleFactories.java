package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.particle.MiasmaParticle;
import net.shirojr.nemuelch.particle.RottenMeatAirParticle;
import net.shirojr.nemuelch.particle.SwipeParticle;

public class NeMuelchParticleFactories {
    static {
        register(NeMuelchParticleTypes.ROTTEN_MEAT_AIR, RottenMeatAirParticle.Factory::new);
        register(NeMuelchParticleTypes.MIASMA, provider -> new MiasmaParticle.Factory());
        register(NeMuelchParticleTypes.MIASMA_RED, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.RED));
        register(NeMuelchParticleTypes.MIASMA_BLUE, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.BLUE));
        register(NeMuelchParticleTypes.MIASMA_GREEN, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.GREEN));
        register(NeMuelchParticleTypes.MIASMA_BROWN, provider -> new MiasmaParticle.Factory(MiasmaItem.ColorPreset.BROWN));
        register(NeMuelchParticleTypes.SWIPE_UP, SwipeParticle.Factory::new);
        register(NeMuelchParticleTypes.SWIPE_DOWN, SwipeParticle.Factory::new);
    }


    @SuppressWarnings("SameParameterValue")
    private static <T extends ParticleEffect> void register(ParticleType<T> type, ParticleFactoryRegistry.PendingParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(type, factory);
    }

    public static void initialize() {
        // static initialisation
    }
}
