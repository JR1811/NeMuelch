package net.shirojr.nemuelch.compat.timewind;

import net.minecraft.world.World;
import ru.aiefu.timeandwindct.TimeAndWindCT;

import java.util.Optional;

public class TimeAndWindAccess {
    public static Optional<Integer> getDayDuration(World world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return Optional.ofNullable(TimeAndWindCT.timeDataMap.get(worldId)).map(storage -> storage.dayDuration);
    }

    public static Optional<Integer> getNightDuration(World world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return Optional.ofNullable(TimeAndWindCT.timeDataMap.get(worldId)).map(storage -> storage.nightDuration);
    }
}
