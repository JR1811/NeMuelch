package net.shirojr.nemuelch.init;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.biome.*;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.data.RegistryKeyHolder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchBiomes {
    List<RegistryKeyHolder<Biome>> ALL = new ArrayList<>();

    RegistryKeyHolder<Biome> BACKYARD = register(
            "backyard",
            new Biome.Builder()
                    .precipitation(false)
                    .temperature(0.5f)
                    .downfall(0.0f)
                    .spawnSettings(SpawnSettings.INSTANCE)
                    .generationSettings(GenerationSettings.INSTANCE)
                    .effects(
                            new BiomeEffects.Builder()
                                    .waterColor(0x3f76e4)
                                    .waterFogColor(0x050533)
                                    .fogColor(0x000000)
                                    .skyColor(0x000000)
                                    .build()
                    )
                    .build()
    );

    RegistryKeyHolder<Biome> ACIDIC_PLAINS = register("acidic_plains",
            new Biome.Builder()
                    .precipitation(true)
                    .temperature(1.5f)
                    .downfall(1.0f)
                    .spawnSettings(SpawnSettings.INSTANCE)
                    .generationSettings(GenerationSettings.INSTANCE)
                    .effects(
                            new BiomeEffects.Builder()
                                    .waterColor(0x076138)
                                    .waterFogColor(0x0f5e13)
                                    .fogColor(0x739e15)
                                    .foliageColor(0x825533)
                                    .grassColor(0x827144)
                                    .skyColor(OverworldBiomeCreator.getSkyColor(1.5f))
                                    .particleConfig(new BiomeParticleConfig(ParticleTypes.WHITE_ASH, 0.015f))
                                    .loopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
                                    .moodSound(new BiomeMoodSound(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
                                    .build()
                    )
                    .build()
    );

    private static RegistryKeyHolder<Biome> register(String name, Biome entry) {
        RegistryKeyHolder<Biome> holder = new RegistryKeyHolder<>(RegistryKey.of(RegistryKeys.BIOME, NeMuelch.getId(name)), entry);
        ALL.add(holder);
        return holder;
    }

    static void bootstrap(Registerable<Biome> registerable) {
        for (RegistryKeyHolder<Biome> holder : ALL) {
            registerable.register(holder.key(), holder.value());
        }
    }

    static void initialize() {
        // static initialisation
    }
}
