package net.shirojr.nemuelch.compat.cca.implementation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.BlightEntityComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;

import java.util.EnumMap;
import java.util.Set;

public class BlightEntityComponentImpl implements BlightEntityComponent {
    private final LivingEntity provider;
    private final EnumMap<BlightType, Severity> blights;

    public BlightEntityComponentImpl(LivingEntity provider) {
        this.provider = provider;
        this.blights = new EnumMap<>(BlightType.class);
    }

    @Override
    public LivingEntity getProvider() {
        return provider;
    }

    @Override
    public Severity getSeverity(BlightType type) {
        return blights.getOrDefault(type, Severity.NONE);
    }

    @Override
    public void setSeverity(BlightType type, Severity severity, boolean disregardSeverityRanking, boolean shouldSync) {
        if (!disregardSeverityRanking) {
            Severity existingSeverity = blights.get(type);
            if (existingSeverity != null && existingSeverity.ordinal() <= severity.ordinal()) {
                return;
            }
        }
        blights.put(type, severity);
        if (shouldSync) this.sync();
    }

    @Override
    public void clearSeverities(boolean shouldSync) {
        blights.forEach((type, severity) -> severity.onCleared(getProvider(), Set.of(type)));
        blights.clear();
        if (shouldSync) this.sync();
    }

    @Override
    public boolean isEmpty() {
        if (this.blights.isEmpty()) return true;
        for (Severity entry : this.blights.values()) {
            if (entry != Severity.NONE) return false;
        }
        return true;
    }

    @Override
    public void tick() {
        World world = provider.getWorld();
        if (world == null) return;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains(NeMuelchNbtKeys.ENTITY_BLIGHTS)) {
            clearSeverities(false);
            NbtCompound nbt = tag.getCompound(NeMuelchNbtKeys.ENTITY_BLIGHTS);
            for (String key : nbt.getKeys()) {
                BlightType type = BlightType.fromString(key);
                Severity severity = Severity.fromString(nbt.getString(key));
                setSeverity(type, severity, true, false);
            }
            this.sync();
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound nbt = new NbtCompound();
        for (var entry : this.blights.entrySet()) {
            nbt.putString(entry.getKey().asString(), entry.getValue().asString());
        }
        tag.put(NeMuelchNbtKeys.ENTITY_BLIGHTS, nbt);
    }
}
