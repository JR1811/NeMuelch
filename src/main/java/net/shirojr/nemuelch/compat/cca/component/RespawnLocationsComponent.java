package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface RespawnLocationsComponent extends Component {
    Identifier KEY = NeMuelch.getId("respawn_locations");

    static RespawnLocationsComponent get(World world) {
        return get(world.getScoreboard());
    }

    static RespawnLocationsComponent get(Scoreboard scoreboard) {
        return NeMuelchComponents.RESPAWN_LOCATIONS.get(scoreboard);
    }

    Map<Identifier, RespawnLocation> getLocations();

    @Nullable
    Identifier getLastLocation();

    void setLastLocation(@Nullable Identifier location);

    default Set<UUID> getAssigned(Identifier location) {
        RespawnLocation respawnLocation = getLocations().get(location);
        if (respawnLocation == null) return Set.of();
        return Collections.unmodifiableSet(respawnLocation.assignedEntities());
    }

    @SuppressWarnings("unused")
    default Set<UUID> getAssigned(RespawnLocation location) {
        return getAssigned(location.identifier());
    }

    default Set<RespawnLocation> getAssigned(UUID uuid) {
        HashSet<RespawnLocation> locations = new HashSet<>();
        for (var entry : getLocations().entrySet()) {
            if (!entry.getValue().assignedEntities().contains(uuid)) continue;
            locations.add(entry.getValue());
        }
        return locations;
    }

    @Nullable
    default RespawnLocation chooseRandomRespawnLocation(Random random, UUID uuid, boolean excludePrevious) {
        List<RespawnLocation> choosableLocations = new ArrayList<>();
        for (RespawnLocation location : getLocations().values()) {
            if (!location.assignedEntities().contains(uuid)) continue;
            if (excludePrevious && getLastLocation() != null) {
                if (location.identifier().equals(getLastLocation())) continue;
            }
            choosableLocations.add(location);
        }
        if (choosableLocations.isEmpty()) {
            setLastLocation(null);
            return null;
        }
        RespawnLocation selectedLocation = choosableLocations.get(random.nextInt(choosableLocations.size()));
        setLastLocation(selectedLocation.identifier());
        return selectedLocation;
    }

    void add(boolean shouldSync, RespawnLocation... locations);

    default void add(RespawnLocation... locations) {
        add(true, locations);
    }

    void remove(List<Identifier> locations);

    /**
     * Leave arguments empty to clear all locations
     */
    default void remove(RespawnLocation... locations) {
        List<Identifier> identifiers = new ArrayList<>();
        if (locations.length == 0) {
            identifiers.addAll(getLocations().keySet());
        }
        for (RespawnLocation location : locations) {
            identifiers.add(location.identifier());
        }
        remove(identifiers);
        sync();
    }

    void assign(RespawnLocation location, UUID target);

    @SuppressWarnings("UnusedReturnValue")
    default boolean assign(Identifier identifier, UUID target) {
        RespawnLocation respawnLocation = getLocations().get(identifier);
        if (respawnLocation == null) return false;
        assign(respawnLocation, target);
        return true;
    }

    void unassign(RespawnLocation location, UUID target);

    @SuppressWarnings("UnusedReturnValue")
    default boolean unassign(Identifier identifier, UUID target) {
        RespawnLocation respawnLocation = getLocations().get(identifier);
        if (respawnLocation == null) return false;
        unassign(respawnLocation, target);
        return true;
    }

    void sync();
}
