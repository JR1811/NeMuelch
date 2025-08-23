package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RespawnLocationsComponentImpl implements RespawnLocationsComponent, AutoSyncedComponent {
    private final Scoreboard provider;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Nullable
    private final MinecraftServer server;
    private final HashMap<Identifier, RespawnLocation> locations;
    @Nullable
    private Identifier lastLocation;

    public RespawnLocationsComponentImpl(Scoreboard provider, @Nullable MinecraftServer server) {
        this.provider = provider;
        this.server = server;
        this.locations = new HashMap<>();
        this.lastLocation = null;
    }

    @Override
    public Map<Identifier, RespawnLocation> getLocations() {
        return Collections.unmodifiableMap(this.locations);
    }

    @Nullable
    @Override
    public Identifier getLastLocation() {
        return this.lastLocation;
    }

    @Override
    public void setLastLocation(@Nullable Identifier lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public void remove(List<Identifier> locations) {
        for (Identifier location : locations) {
            this.locations.remove(location);
        }
        sync();
    }

    @Override
    public void add(boolean shouldSync, RespawnLocation... locations) {
        for (RespawnLocation location : locations) {
            this.locations.put(location.identifier(), location);
        }
        if (shouldSync) {
            sync();
        }
    }

    @Override
    public void assign(RespawnLocation location, UUID target) {
        this.locations.computeIfAbsent(location.identifier(), identifier -> location).assignedEntities().add(target);
        sync();
    }

    @Override
    public void unassign(RespawnLocation location, UUID target) {
        RespawnLocation respawnLocation = this.locations.get(location.identifier());
        if (respawnLocation == null) return;
        respawnLocation.assignedEntities().remove(target);
        sync();
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("locations")) {
            locations.clear();
            NbtList locationsNbtList = nbt.getList("locations", NbtElement.COMPOUND_TYPE);
            for (NbtElement nbtElement : locationsNbtList) {
                NbtCompound locationNbt = (NbtCompound) nbtElement;
                RespawnLocation respawnLocation = RespawnLocation.fromNbt(locationNbt);
                if (respawnLocation != null) {
                    add(respawnLocation);
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList locationsNbtList = new NbtList();
        for (RespawnLocation entry : this.locations.values()) {
            NbtCompound locationNbt = new NbtCompound();
            entry.toNbt(locationNbt);
            locationsNbtList.add(locationNbt);
        }
        nbt.put("locations", locationsNbtList);
    }

    @Override
    public void sync() {
        NeMuelchComponents.RESPAWN_LOCATIONS.sync(this.provider);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.hasPermissionLevel(2);
    }
}
