package net.shirojr.nemuelch.util.helper;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.Nullable;

public enum SoundInstanceHelper {
    TNT_STICK("burning_tnt_stick"),
    OMINOUS_HEART("beating_heart"),
    WHISPERS("whispers"),
    DROP_POT("drop_pot");

    private final Identifier identifier;

    SoundInstanceHelper(String name) {
        this.identifier = new Identifier(NeMuelch.MOD_ID, name);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    @Nullable
    public static SoundInstanceHelper fromIdentifier(Identifier identifier) {
        for (SoundInstanceHelper instance : SoundInstanceHelper.values()) {
            if (instance.getIdentifier().equals(identifier)) return instance;
        }
        return null;
    }
}
