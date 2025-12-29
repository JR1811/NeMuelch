package net.shirojr.nemuelch.compat.timewind;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.function.Function;

public enum Phase implements StringIdentifiable {
    DAY(SafeTimeHandler::getDayDuration),
    NIGHT(SafeTimeHandler::getNightDuration);

    private final Function<World, Long> duration;

    public static final com.mojang.serialization.Codec<Phase> CODEC = StringIdentifiable.createCodec(Phase::values);

    Phase(Function<World, Long> duration) {
        this.duration = duration;
    }

    public long getDuration(World world) {
        return this.duration.apply(world);
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
