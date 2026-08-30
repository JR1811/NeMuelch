package net.shirojr.nemuelch.compat.cca.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Defines a prism-shaped object, made out of multiple vertices. Min and Max height are defined by lowest and highest
 * vertex entry.
 */
public class ComplexZone {
    private static final int MAX_CACHE_SIZE = 5000;

    private final List<Vec3d> vertices;
    private @Nullable Box boundingBox;
    private final Object2BooleanLinkedOpenHashMap<Vec3d> isInZoneCache = new Object2BooleanLinkedOpenHashMap<>();
    private final HashMap<UUID, SoundEvent> zoneListeners;
    private final ZoneChangeListener changeListener;
    private final Identifier identifier;

    public ComplexZone(Identifier identifier, ZoneChangeListener changeListener) {
        this.identifier = identifier;
        this.vertices = new ArrayList<>();
        this.zoneListeners = new HashMap<>();
        this.boundingBox = null;
        this.changeListener = changeListener;
    }

    public ComplexZone(Identifier identifier, List<Vec3d> vertices, HashMap<UUID, SoundEvent> zoneListeners, ZoneChangeListener changeListener) {
        this.identifier = identifier;
        this.vertices = new ArrayList<>(vertices);
        this.zoneListeners = new HashMap<>(zoneListeners);
        this.changeListener = changeListener;
        this.recalculateBoundingBox();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Map<UUID, SoundEvent> getListeners() {
        return Collections.unmodifiableMap(this.zoneListeners);
    }

    public boolean modifyZoneListeners(Consumer<HashMap<UUID, SoundEvent>> modifier) {
        HashMap<UUID, SoundEvent> old = new HashMap<>(this.zoneListeners);
        modifier.accept(this.zoneListeners);
        return !old.equals(this.zoneListeners);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean containsZoneListener(UUID zoneListener) {
        return this.zoneListeners.containsKey(zoneListener);
    }

    public List<Vec3d> getVertices() {
        return Collections.unmodifiableList(this.vertices);
    }

    public List<Vec3d> getAllVertices(boolean connectWithFirst, boolean flip) {
        List<Vec3d> result = new ArrayList<>();
        Box box = this.getBoundingBox();
        if (box == null) return result;
        Vec3d first = null;
        for (Vec3d vertex : this.getVertices()) {
            Vec3d a = new Vec3d(vertex.x, box.minY, vertex.z);
            Vec3d b = new Vec3d(vertex.x, box.maxY, vertex.z);
            result.add(flip ? b : a);
            result.add(flip ? a : b);
            if (first == null && connectWithFirst) {
                first = a;
            }
        }
        if (first != null) {
            result.add(first);
        }
        return result;
    }

    public boolean modifyVertices(Consumer<List<Vec3d>> modifier) {
        List<Vec3d> old = new ArrayList<>(this.vertices);
        modifier.accept(this.vertices);
        if (old.equals(this.vertices)) return false;
        this.recalculateBoundingBox();
        this.isInZoneCache.clear();
        this.changeListener.onZoneContentChanged(this, true);
        return true;
    }

    public @Nullable Box getBoundingBox() {
        return boundingBox;
    }

    public boolean isEmpty() {
        return this.vertices.isEmpty();
    }

    private void recalculateBoundingBox() {
        if (this.vertices.isEmpty()) {
            this.boundingBox = null;
            return;
        }

        Vec3d min = new Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        Vec3d max = new Vec3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        for (Vec3d vertex : this.vertices) {
            min = new Vec3d(Math.min(min.x, vertex.x), Math.min(min.y, vertex.y), Math.min(min.z, vertex.z));
            max = new Vec3d(Math.max(max.x, vertex.x), Math.max(max.y, vertex.y), Math.max(max.z, vertex.z));
        }

        this.boundingBox = new Box(min, max);
    }

    public boolean contains(Vec3d pos) {
        if (this.isInZoneCache.containsKey(pos)) {
            return this.isInZoneCache.getBoolean(pos);
        }
        if (this.isEmpty() || this.boundingBox == null) return false;
        if (!this.boundingBox.contains(pos)) return false;

        boolean isInside = this.computeIsInZone(pos);
        if (this.isInZoneCache.size() >= MAX_CACHE_SIZE) {
            this.isInZoneCache.removeFirstBoolean();
        }
        this.isInZoneCache.put(pos, isInside);
        return isInside;
    }

    public boolean contains(BlockPos pos) {
        return this.contains(pos.toCenterPos());
    }

    private boolean computeIsInZone(Vec3d pos) {
        boolean isInside = false;
        for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
            Vec3d a = vertices.get(j);
            Vec3d b = vertices.get(i);
            if ((a.z > pos.z) != (b.z > pos.z)) {
                double intersectionX = a.x + (pos.z - a.z) * (b.x - a.x) / (b.z - a.z);
                if (intersectionX > pos.x) {
                    isInside = !isInside;
                }
            }
        }
        return isInside;
    }

    private LongSet getContainingChunks() {
        LongSet result = new LongOpenHashSet();
        if (this.isEmpty()) return result;
        Box box = this.getBoundingBox();
        if (box == null) return result;
        int minChunkX = MathHelper.floor(box.minX) >> 4;
        int maxChunkX = MathHelper.floor(box.maxX) >> 4;
        int minChunkZ = MathHelper.floor(box.minZ) >> 4;
        int maxChunkZ = MathHelper.floor(box.maxZ) >> 4;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                result.add(ChunkPos.toLong(x, z));
            }
        }
        return result;
    }

    public static ComplexZone fromNbt(NbtCompound nbt, ZoneChangeListener listener) {
        String zoneKey = nbt.getString(NeMuelchNbtKeys.IDENTIFIER);
        Identifier identifier = Identifier.tryParse(zoneKey);
        if (identifier == null) throw new NullPointerException("Invalid Zone ID: " + zoneKey);

        List<Vec3d> vertices = new ArrayList<>();
        NbtList verticesNbt = nbt.getList(NeMuelchNbtKeys.VERTICES, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < verticesNbt.size(); i++) {
            Vec3d vertex = Vec3dHelper.fromNbt(verticesNbt.getCompound(i));
            if (vertex == null) continue;
            vertices.add(vertex);
        }

        HashMap<UUID, SoundEvent> listeners = new HashMap<>();
        NbtList listenersNbt = nbt.getList(NeMuelchNbtKeys.LISTENERS, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < listenersNbt.size(); i++) {
            NbtCompound entryNbt = listenersNbt.getCompound(i);
            NbtElement listenerUuidNbt = entryNbt.get(NeMuelchNbtKeys.LISTENER);
            if (listenerUuidNbt == null) continue;
            UUID listenerUuid = NbtHelper.toUuid(listenerUuidNbt);
            Identifier soundId = null;
            if (entryNbt.contains(NeMuelchNbtKeys.SOUND)) {
                soundId = Identifier.tryParse(entryNbt.getString(NeMuelchNbtKeys.SOUND));
            }
            SoundEvent sound = soundId == null ? null : Registries.SOUND_EVENT.get(soundId);
            listeners.put(listenerUuid, sound);
        }
        return new ComplexZone(identifier, vertices, listeners, listener);
    }

    public void toNbt(NbtCompound nbt) {
        nbt.putString(NeMuelchNbtKeys.IDENTIFIER, this.identifier.toString());

        NbtList verticesNbt = new NbtList();
        for (Vec3d vertex : this.vertices) {
            NbtCompound vertexNbt = new NbtCompound();
            Vec3dHelper.toNbt(vertexNbt, vertex);
            verticesNbt.add(vertexNbt);
        }
        nbt.put(NeMuelchNbtKeys.VERTICES, verticesNbt);

        NbtList listenersNbt = new NbtList();
        for (var entry : this.zoneListeners.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.put(NeMuelchNbtKeys.LISTENER, NbtHelper.fromUuid(entry.getKey()));
            SoundEvent sound = entry.getValue();
            if (sound != null) {
                entryNbt.putString(NeMuelchNbtKeys.SOUND, sound.getId().toString());
            }
            listenersNbt.add(entryNbt);
        }
        nbt.put(NeMuelchNbtKeys.LISTENERS, listenersNbt);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComplexZone zone)) return false;
        return Objects.equals(getIdentifier(), zone.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getIdentifier());
    }

    @FunctionalInterface
    public interface ZoneChangeListener {
        void onZoneContentChanged(ComplexZone zone, boolean shouldSync);
    }

    public static class Index {
        private final Long2ObjectOpenHashMap<HashSet<ComplexZone>> chunkBuckets = new Long2ObjectOpenHashMap<>();
        private final Map<ComplexZone, LongSet> zoneToChunks = new IdentityHashMap<>();
        private final HashMap<UUID, HashSet<ComplexZone>> listenerToZones = new HashMap<>();

        public void add(ComplexZone zone) {
            LongSet chunks = zone.getContainingChunks();
            zoneToChunks.put(zone, chunks);
            for (long chunkKey : chunks) {
                chunkBuckets.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(zone);
            }
            zone.getListeners().keySet().forEach(uuid ->
                    listenerToZones.computeIfAbsent(uuid, entryUuid -> new HashSet<>()).add(zone)
            );
        }

        public void remove(ComplexZone zone) {
            LongSet removedChunks = zoneToChunks.remove(zone);
            if (removedChunks == null) return;
            for (long chunkKey : removedChunks) {
                HashSet<ComplexZone> bucket = this.chunkBuckets.get(chunkKey);
                if (bucket != null) {
                    bucket.remove(zone);
                }
            }
            for (UUID listener : zone.getListeners().keySet()) {
                HashSet<ComplexZone> entries = this.listenerToZones.get(listener);
                if (entries == null) continue;
                entries.removeIf(entry -> entry.equals(zone));
            }
        }

        public Set<ComplexZone> getChunkBucketHits(Vec3d pos) {
            BlockPos blockPos = BlockPos.ofFloored(pos);
            long chunkKey = ChunkPos.toLong(blockPos);
            HashSet<ComplexZone> bucket = this.chunkBuckets.get(chunkKey);
            return bucket == null ? Collections.emptySet() : Collections.unmodifiableSet(bucket);
        }

        public Set<ComplexZone> getListenedZones(UUID listenerUuid) {
            HashSet<ComplexZone> zones = this.listenerToZones.get(listenerUuid);
            if (zones == null) return Collections.emptySet();
            return Collections.unmodifiableSet(zones);
        }

        public Set<ComplexZone> getZones() {
            return Collections.unmodifiableSet(this.zoneToChunks.keySet());
        }

        public void refresh(ComplexZone zone) {
            this.remove(zone);
            this.add(zone);
        }

        public void clear() {
            this.chunkBuckets.clear();
            this.zoneToChunks.clear();
            this.listenerToZones.clear();
        }
    }
}
