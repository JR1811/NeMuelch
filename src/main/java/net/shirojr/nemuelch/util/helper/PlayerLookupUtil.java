package net.shirojr.nemuelch.util.helper;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.HashSet;

public class PlayerLookupUtil {
    private PlayerLookupUtil() {
    }

    public static Collection<ServerPlayerEntity> trackingAndSelf(Entity target) {
        HashSet<ServerPlayerEntity> output = new HashSet<>(PlayerLookup.tracking(target));
        if (target instanceof ServerPlayerEntity serverPlayer) output.add(serverPlayer);
        return output;
    }
}
