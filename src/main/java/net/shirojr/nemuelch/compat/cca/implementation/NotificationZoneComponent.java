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
import net.shirojr.nemuelch.compat.cca.util.NotificationZone;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class NotificationZoneComponent implements Component, AutoSyncedComponent, NotificationZone.ZoneChangeListener {
    public static final Identifier KEY = NeMuelch.getId("notification_zone");
    private final World world;
    private final LinkedHashMap<Identifier, NotificationZone> zones = new LinkedHashMap<>();
    private final NotificationZone.Index zoneIndexing = new NotificationZone.Index();

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

    public boolean containsKey(Identifier id) {
        return this.zones.containsKey(id);
    }

    public boolean zoneContainsVertex(Identifier zoneId, Vec3d vertex) {
        NotificationZone zone = this.getZone(zoneId);
        if (zone == null) return false;
        return zone.getVertices().contains(vertex);
    }

    @SuppressWarnings("UnusedReturnValue")
    public NotificationZone createZone(Identifier id) {
        return this.createZone(id, null);
    }

    /**
     *
     * @param id            zone identifier key
     * @param insertionZone use <code>null</code> to insert an empty new zone
     * @return the registered zone, or <code>null</code> if the identifier was already present
     */
    @Nullable
    public NotificationZone createZone(Identifier id, @Nullable NotificationZone insertionZone) {
        if (this.zones.containsKey(id)) {
            return null;
        }
        NotificationZone zone = insertionZone == null ? new NotificationZone(id, this) : insertionZone;
        this.zones.put(id, zone);
        this.zoneIndexing.add(zone);
        this.sync();
        return zone;
    }

    public boolean removeZone(Identifier id) {
        NotificationZone removedEntry = this.zones.remove(id);
        if (removedEntry == null) return false;
        this.zoneIndexing.remove(removedEntry);
        this.sync();
        return true;
    }

    @Nullable
    public NotificationZone getZone(Identifier id) {
        return this.zones.get(id);
    }

    public Collection<NotificationZone> getZones() {
        return Collections.unmodifiableCollection(this.zones.values());
    }

    public HashSet<NotificationZone> getZones(UUID zoneListener) {
        HashSet<NotificationZone> result = new HashSet<>();
        for (NotificationZone entry : this.zones.values()) {
            if (!entry.containsZoneListener(zoneListener)) continue;
            result.add(entry);
        }
        return result;
    }

    public Set<NotificationZone> getChunkBucketHits(Vec3d pos) {
        return this.zoneIndexing.getChunkBucketHits(pos);
    }

    public Set<NotificationZone> getChunkBucketHits(BlockPos pos) {
        return this.zoneIndexing.getChunkBucketHits(pos.toCenterPos());
    }

    public HashSet<NotificationZone> getContainingZones(Vec3d pos) {
        HashSet<NotificationZone> result = new HashSet<>();
        for (NotificationZone chunkBucketHit : this.getChunkBucketHits(pos)) {
            if (chunkBucketHit.contains(pos)) {
                result.add(chunkBucketHit);
            }
        }
        return result;
    }

    public HashSet<NotificationZone> getContainingZones(BlockPos pos) {
        HashSet<NotificationZone> result = new HashSet<>();
        for (NotificationZone chunkBucketHit : this.getChunkBucketHits(pos)) {
            if (chunkBucketHit.contains(pos)) {
                result.add(chunkBucketHit);
            }
        }
        return result;
    }


    public void modifyZoneVertices(Identifier id, Consumer<List<Vec3d>> modifier) {
        NotificationZone zone = this.zones.get(id);
        if (zone == null) return;
        if (zone.modifyVertices(modifier)) {
            this.sync();
        }
    }

    public boolean addZoneListener(Identifier zoneId, UUID listenerUuid, @Nullable SoundEvent sound) {
        NotificationZone zone = this.zones.get(zoneId);
        if (zone == null) return false;
        boolean added = zone.modifyZoneListeners(modifier -> modifier.put(listenerUuid, sound));
        if (added) {
            this.sync();
        }
        return added;
    }

    public boolean removeZoneListener(Identifier zoneId, UUID listenerUuid) {
        NotificationZone zone = this.zones.get(zoneId);
        if (zone == null) return false;
        boolean removed = zone.modifyZoneListeners(modifier -> modifier.remove(listenerUuid));
        if (removed) this.sync();
        return removed;
    }

    public boolean modifyNotificationSound(Identifier zoneId, UUID zoneListener, @Nullable SoundEvent sound) {
        NotificationZone zone = this.getZone(zoneId);
        if (zone == null) return false;
        if (!zone.containsZoneListener(zoneListener)) return false;
        boolean modified = zone.modifyZoneListeners(modifier -> modifier.put(zoneListener, sound));
        if (modified) {
            this.sync();
        }
        return modified;
    }

    public void refreshIndexing() {
        this.zones.values().forEach(this::onZoneContentChanged);
    }

    @Override
    public void onZoneContentChanged(NotificationZone zone) {
        this.zoneIndexing.refresh(zone);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.zones.clear();
        this.zoneIndexing.clear();

        NbtList zonesNbt = tag.getList(NeMuelchNbtKeys.NOTIFICATION_ZONES, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < zonesNbt.size(); i++) {
            NbtCompound entryNbt = zonesNbt.getCompound(i);
            NotificationZone zone = NotificationZone.fromNbt(entryNbt, this);
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
