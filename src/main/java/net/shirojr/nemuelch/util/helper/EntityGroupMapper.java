package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.EntityGroup;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum EntityGroupMapper implements StringIdentifiable {
    DEFAULT(EntityGroup.DEFAULT),
    UNDEAD(EntityGroup.UNDEAD),
    ARTHROPOD(EntityGroup.ARTHROPOD),
    ILLAGER(EntityGroup.ILLAGER),
    AQUATIC(EntityGroup.AQUATIC);

    private final EntityGroup group;

    EntityGroupMapper(EntityGroup group) {
        this.group = group;
    }

    public static EntityGroupMapper get(String name) {
        for (EntityGroupMapper entry : EntityGroupMapper.values()) {
            if (entry.asString().equals(name.toLowerCase(Locale.ROOT))) {
                return entry;
            }
        }
        return DEFAULT;
    }

    public EntityGroup getGroup() {
        return group;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
