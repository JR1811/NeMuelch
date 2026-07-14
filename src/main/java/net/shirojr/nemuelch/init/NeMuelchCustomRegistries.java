package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.occasion.util.OccasionType;

public interface NeMuelchCustomRegistries {
    RegistryKey<Registry<OccasionType>> OCCASIONS_REGISTRY_KEY = RegistryKey.ofRegistry(NeMuelch.getId("occasions"));
    SimpleRegistry<OccasionType> OCCASIONS = registerRegistry(OCCASIONS_REGISTRY_KEY);

    RegistryKey<Registry<AbstractMonsterType>> MONSTERS_KEY = RegistryKey.ofRegistry(NeMuelch.getId("monsters"));
    SimpleRegistry<AbstractMonsterType> MONSTERS = registerRegistry(MONSTERS_KEY);

    @SuppressWarnings("SameParameterValue")
    private static <T> SimpleRegistry<T> registerRegistry(RegistryKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    }

    static void initialize() {
        // static initialisation
    }
}
