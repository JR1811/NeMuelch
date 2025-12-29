package net.shirojr.nemuelch.occasion.util;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum OccasionState implements StringIdentifiable {
    DISABLED, ACTIVE, INACTIVE, FINISHED;

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
