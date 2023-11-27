package net.shirojr.nemuelch.util.helper;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.Nullable;

public enum SoundInstanceHelper {
    TNT_STICK(new Identifier(NeMuelch.MOD_ID, "burning_tnt_stick")),
    OMINOUS_HEART(new Identifier(NeMuelch.MOD_ID, "beating_heart"));

    private final Identifier identifier;

    SoundInstanceHelper(Identifier identifier) {
        this.identifier = identifier;
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
