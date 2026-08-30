package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.ComplexZone;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class NotificationZoneComponent implements Component, AutoSyncedComponent, ComplexZone.ZoneChangeListener {
    public static final Identifier KEY = NeMuelch.getId("notification_zone");
    private final World world;
    private final LinkedHashMap<Identifier, ComplexZone> zones = new LinkedHashMap<>();
    private final ComplexZone.Index zoneIndexing = new ComplexZone.Index();

    public NotificationZoneComponent(World world) {
        this.world = world;
    }

    public static NotificationZoneComponent get(World world) {
        return NeMuelchComponents.NOTIFICATION_ZONE.get(world);
    }

    public World getWorld() {
        return world;
    }

    public List<Identifier> getRegistered() {
        List<Identifier> result = new ArrayList<>();
        this.zones.forEach((identifier, zone) -> result.add(identifier));
        return result;
    }

    public Set<ComplexZone> getListenedNotificationZones(UUID listenerUuid) {
        return this.zoneIndexing.getListenedZones(listenerUuid);
    }

    public boolean containsKey(Identifier id) {
        return this.zones.containsKey(id);
    }

    public boolean zoneContainsVertex(Identifier zoneId, Vec3d vertex) {
        ComplexZone zone = this.getZone(zoneId);
        if (zone == null) return false;
        return zone.getVertices().contains(vertex);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ComplexZone createZone(Identifier id) {
        return this.createZone(id, null);
    }

    /**
     *
     * @param id            zone identifier key
     * @param insertionZone use <code>null</code> to insert an empty new zone
     * @return the registered zone, or <code>null</code> if the identifier was already present
     */
    @Nullable
    public ComplexZone createZone(Identifier id, @Nullable ComplexZone insertionZone) {
        if (this.zones.containsKey(id)) {
            return null;
        }
        ComplexZone zone = insertionZone == null ? new ComplexZone(id, this) : insertionZone;
        this.zones.put(id, zone);
        this.zoneIndexing.add(zone);
        this.sync();
        return zone;
    }

    public boolean removeZone(Identifier id) {
        ComplexZone removedEntry = this.zones.remove(id);
        if (removedEntry == null) return false;
        this.zoneIndexing.remove(removedEntry);
        this.sync();
        return true;
    }

    @Nullable
    public ComplexZone getZone(Identifier id) {
        return this.zones.get(id);
    }

    public Collection<ComplexZone> getZones() {
        return Collections.unmodifiableCollection(this.zones.values());
    }

    public HashSet<ComplexZone> getZones(UUID zoneListener) {
        HashSet<ComplexZone> result = new HashSet<>();
        for (ComplexZone entry : this.zones.values()) {
            if (!entry.containsZoneListener(zoneListener)) continue;
            result.add(entry);
        }
        return result;
    }

    public Set<ComplexZone> getChunkBucketHits(Vec3d pos) {
        return this.zoneIndexing.getChunkBucketHits(pos);
    }

    public Set<ComplexZone> getChunkBucketHits(BlockPos pos) {
        return this.getChunkBucketHits(pos.toCenterPos());
    }

    public HashSet<ComplexZone> getContainingZones(Vec3d pos) {
        HashSet<ComplexZone> result = new HashSet<>();
        for (ComplexZone chunkBucketHit : this.getChunkBucketHits(pos)) {
            if (chunkBucketHit.contains(pos)) {
                result.add(chunkBucketHit);
            }
        }
        return result;
    }

    public HashSet<ComplexZone> getContainingZones(BlockPos pos) {
        HashSet<ComplexZone> result = new HashSet<>();
        for (ComplexZone chunkBucketHit : this.getChunkBucketHits(pos)) {
            if (chunkBucketHit.contains(pos)) {
                result.add(chunkBucketHit);
            }
        }
        return result;
    }


    public void modifyZoneVertices(Identifier id, Consumer<List<Vec3d>> modifier) {
        ComplexZone zone = this.zones.get(id);
        if (zone == null) return;
        if (zone.modifyVertices(modifier)) {
            this.sync();
        }
    }

    public boolean addZoneListener(Identifier zoneId, UUID listenerUuid, @Nullable SoundEvent sound) {
        ComplexZone zone = this.zones.get(zoneId);
        if (zone == null) return false;
        boolean added = zone.modifyZoneListeners(modifier -> modifier.put(listenerUuid, sound));
        if (added) {
            this.zoneIndexing.refresh(zone);
            this.sync();
        }
        return added;
    }

    public boolean removeZoneListener(Identifier zoneId, UUID listenerUuid) {
        ComplexZone zone = this.zones.get(zoneId);
        if (zone == null) return false;
        boolean removed = zone.modifyZoneListeners(modifier -> modifier.remove(listenerUuid));
        if (removed) {
            this.zoneIndexing.refresh(zone);
            this.sync();
        }
        return removed;
    }

    public boolean modifyNotificationSound(Identifier zoneId, UUID zoneListener, @Nullable SoundEvent sound) {
        ComplexZone zone = this.getZone(zoneId);
        if (zone == null) return false;
        if (!zone.containsZoneListener(zoneListener)) return false;
        boolean modified = zone.modifyZoneListeners(modifier -> modifier.put(zoneListener, sound));
        if (modified) {
            this.sync();
        }
        return modified;
    }

    @Override
    public void onZoneContentChanged(ComplexZone zone, boolean shouldSync) {
        this.zoneIndexing.refresh(zone);
        if (shouldSync) this.sync();
    }

    public void refreshIndexing() {
        this.zones.values().forEach(zone -> this.onZoneContentChanged(zone, false));
        this.sync();
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.zones.clear();
        this.zoneIndexing.clear();

        NbtList zonesNbt = tag.getList(NeMuelchNbtKeys.NOTIFICATION_ZONES, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < zonesNbt.size(); i++) {
            NbtCompound entryNbt = zonesNbt.getCompound(i);
            ComplexZone zone = ComplexZone.fromNbt(entryNbt, this);
            this.zones.put(zone.getIdentifier(), zone);
        }
        this.refreshIndexing();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList zonesNbt = new NbtList();
        for (var entry : this.zones.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entry.getValue().toNbt(entryNbt);
            zonesNbt.add(entryNbt);
        }
        tag.put(NeMuelchNbtKeys.NOTIFICATION_ZONES, zonesNbt);
    }

    public void sync() {
        if (!(this.world instanceof ServerWorld)) return;
        NeMuelchComponents.NOTIFICATION_ZONE.sync(this.world);
    }
}
