package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.FadeZone;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocationalFadeComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("locational_fade");

    private final World world;
    private final HashMap<Identifier, FadeZone> zones;

    public LocationalFadeComponent(World world) {
        this.world = world;
        this.zones = new HashMap<>();
    }

    public static LocationalFadeComponent get(World world) {
        return NeMuelchComponents.LOCATIONAL_FADE.get(world);
    }

    public boolean put(FadeZone zone) {
        if (this.zones.containsKey(zone.identifier())) return false;
        this.zones.put(zone.identifier(), zone);
        this.sync();
        return true;
    }

    public boolean remove(Identifier zoneId) {
        if (!this.zones.containsKey(zoneId)) return false;
        this.zones.remove(zoneId);
        this.sync();
        return true;
    }

    public void removeAll() {
        this.zones.clear();
        this.sync();
    }

    public Map<Identifier, FadeZone> getZones() {
        return Collections.unmodifiableMap(this.zones);
    }

    public int size() {
        return this.zones.size();
    }

    public double getCombinedFade(PlayerEntity player) {
        UUID targetUuid = player.getUuid();
        Vec3d targetPos = player.getPos();
        DoubleArrayList applicableZoneValues = new DoubleArrayList();
        for (FadeZone zone : this.zones.values()) {
            if (!zone.isGlobal() && !zone.targets().contains(targetUuid)) continue;
            double fade = zone.getNormalizedFade(targetPos);
            if (fade <= 0) continue;
            applicableZoneValues.add(fade);
        }
        if (applicableZoneValues.isEmpty()) return 0f;
        double lowestFade = 1f;
        for (double entryFade : applicableZoneValues) {
            if (entryFade < lowestFade) lowestFade = entryFade;
        }
        return MathHelper.clamp(lowestFade, 0, 1);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.zones.clear();
        NbtList zonesNbt = tag.getList(NeMuelchNbtKeys.ZONES, NbtElement.COMPOUND_TYPE);
        for (NbtElement entryNbt : zonesNbt) {
            FadeZone zone = FadeZone.fromNbt((NbtCompound) entryNbt);
            if (zone == null) continue;
            this.zones.put(zone.identifier(), zone);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList zonesNbt = new NbtList();
        for (FadeZone zone : this.zones.values()) {
            NbtCompound zoneNbt = new NbtCompound();
            zone.toNbt(zoneNbt);
            zonesNbt.add(zoneNbt);
        }
        tag.put(NeMuelchNbtKeys.ZONES, zonesNbt);
    }

    public void sync() {
        if (this.world.isClient()) return;
        NeMuelchComponents.LOCATIONAL_FADE.sync(this.world);
    }
}
