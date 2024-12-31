package net.shirojr.nemuelch.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface RestrictedRendering {
    boolean nemuelch$isIllusion();

    void nemuelch$setIllusion(boolean isIllusion);

    List<UUID> nemuelch$getPersistentIllusionTargets();

    List<Entity> nemuelch$getIllusionTargets();

    void nemuelch$modifyIllusionTargets(Consumer<List<UUID>> newList, boolean sendClientUpdate);

    default void nemuelch$modifyIllusionTargets(Consumer<List<UUID>> newList) {
        nemuelch$modifyIllusionTargets(newList, true);
    }

    void nemuelch$clearIllusionTargets();

    void nemuelch$updateClients();

    void nemuelch$updateTrackedEntityIds(ServerWorld world);
}
