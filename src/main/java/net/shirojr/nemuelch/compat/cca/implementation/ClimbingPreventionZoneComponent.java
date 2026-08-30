package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
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

public class ClimbingPreventionZoneComponent implements Component, AutoSyncedComponent, ComplexZone.ZoneChangeListener {
    public static final Identifier KEY = NeMuelch.getId("climbing_prevention_zone");
    private final World world;

    private final LinkedHashMap<Identifier, ComplexZone> zones = new LinkedHashMap<>();
    private final ComplexZone.Index zoneIndexing = new ComplexZone.Index();

    public ClimbingPreventionZoneComponent(World world) {
        this.world = world;
    }

    public static ClimbingPreventionZoneComponent get(World world) {
        return NeMuelchComponents.CLIMBING_PREVENTION_ZONE.get(world);
    }

    public List<Identifier> getRegistered() {
        List<Identifier> result = new ArrayList<>();
        this.zones.forEach((identifier, zone) -> result.add(identifier));
        return result;
    }

    public boolean containsKey(Identifier id) {
        return this.zones.containsKey(id);
    }

    public boolean isPreventedAtAny(Collection<BlockPos> positions) {
        boolean anyPrevention = false;
        for (BlockPos pos : positions) {
            Vec3d centerPos = pos.toCenterPos();
            for (ComplexZone bucketHit : this.zoneIndexing.getChunkBucketHits(centerPos)) {
                if (!bucketHit.contains(centerPos)) continue;
                anyPrevention = true;
                break;
            }
        }
        return anyPrevention;
    }

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

    @SuppressWarnings("UnusedReturnValue")
    @Nullable
    public ComplexZone createZone(Identifier id) {
        return this.createZone(id, null);
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

    public Set<ComplexZone> getZones() {
        return this.zoneIndexing.getZones();
    }

    public void modifyZoneVertices(Identifier id, Consumer<List<Vec3d>> modifier) {
        ComplexZone zone = this.zones.get(id);
        if (zone == null) return;
        if (zone.modifyVertices(modifier)) {
            this.sync();
        }
    }

    public boolean zoneContainsVertex(Identifier zoneId, Vec3d vertex) {
        ComplexZone zone = this.getZone(zoneId);
        if (zone == null) return false;
        return zone.getVertices().contains(vertex);
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
        NeMuelchComponents.CLIMBING_PREVENTION_ZONE.sync(this.world);
    }
}
