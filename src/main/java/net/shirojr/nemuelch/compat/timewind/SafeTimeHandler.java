package net.shirojr.nemuelch.compat.timewind;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;

public class SafeTimeHandler {
    public static final long DEFAULT_DAY_DURATION = 12_000;
    public static final long DEFAULT_NIGHT_DURATION = 12_000;

    public static boolean isTimeWindPresent() {
        return FabricLoader.getInstance().isModLoaded("tawct");
    }

    public static long getDayDuration(World world) {
        if (isTimeWindPresent()) {
            return TimeAndWindAccess.getDayDuration(world).map(Integer::longValue).orElse(DEFAULT_DAY_DURATION);
        }
        return DEFAULT_DAY_DURATION;
    }

    public static long getNightDuration(World world) {
        if (isTimeWindPresent()) {
            return TimeAndWindAccess.getNightDuration(world).map(Integer::longValue).orElse(DEFAULT_NIGHT_DURATION);
        }
        return DEFAULT_NIGHT_DURATION;
    }
}
