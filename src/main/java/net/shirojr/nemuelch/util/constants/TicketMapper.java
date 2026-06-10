package net.shirojr.nemuelch.util.constants;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum TicketMapper implements StringIdentifiable {
    FULL(22),
    BLOCK_TICKING(23),
    BORDER(24),
    INACCESSIBLE(33);

    @SuppressWarnings("deprecation")
    public static final Codec<TicketMapper> CODEC = StringIdentifiable.createCodec(TicketMapper::values);


    private final int level;

    TicketMapper(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static TicketMapper fromLevel(int level) {
        TicketMapper output = FULL;
        for (TicketMapper value : TicketMapper.values()) {
            if (value.getLevel() > level) return output;
            output = value;
        }
        return INACCESSIBLE;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
