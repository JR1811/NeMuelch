package net.shirojr.nemuelch.occasion.util;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum OccasionGrade implements StringIdentifiable {
    DANGEROUS, NEUTRAL, BENEFICIAL;

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
