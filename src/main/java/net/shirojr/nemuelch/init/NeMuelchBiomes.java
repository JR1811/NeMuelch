package net.shirojr.nemuelch.init;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
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
                    .effects(
                            new BiomeEffects.Builder()
                                    .waterColor(0x3f76e4)
                                    .waterFogColor(0x050533)
                                    .fogColor(0x000000)
                                    .skyColor(0x000000)
                                    .build()
                    )
                    .spawnSettings(SpawnSettings.INSTANCE)
                    .generationSettings(GenerationSettings.INSTANCE)
                    .build()
    );

    @SuppressWarnings("SameParameterValue")
    private static RegistryKeyHolder<Biome> register(String name, Biome entry) {
        RegistryKeyHolder<Biome> holder = new RegistryKeyHolder<>(RegistryKey.of(RegistryKeys.BIOME, NeMuelch.getId(name)), entry);
        ALL.add(holder);
        return holder;
    }

    static void initialize() {
        // static initialisation
    }
}
