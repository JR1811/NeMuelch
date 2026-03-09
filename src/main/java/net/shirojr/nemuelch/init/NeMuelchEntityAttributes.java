package net.shirojr.nemuelch.init;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

import java.util.HashSet;

@SuppressWarnings("SameParameterValue")
public interface NeMuelchEntityAttributes {
    HashSet<EntityAttribute> ALL_ATTRIBUTES = new HashSet<>();

    EntityAttribute BIND_RADIUS = register("bind_radius", 6, 0.5, 30);

    private static ClampedEntityAttribute register(String name, double fallback, double min, double max) {
        ClampedEntityAttribute registeredEntry = Registry.register(
                Registries.ATTRIBUTE,
                NeMuelch.getId(name),
                new ClampedEntityAttribute(getTranslationKey(name), fallback, min, max)
        );
        ALL_ATTRIBUTES.add(registeredEntry);
        return registeredEntry;
    }

    private static String getTranslationKey(String name) {
        return "attribute.%s.%s".formatted(NeMuelch.MOD_ID, name);
    }

    static void initialize() {
        // static initialisation
    }
}
