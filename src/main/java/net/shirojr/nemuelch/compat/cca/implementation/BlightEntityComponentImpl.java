package net.shirojr.nemuelch.compat.cca.implementation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.BlightEntityComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.EnumMap;

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
    public void setSeverity(BlightType type, Severity severity, boolean shouldSync) {
        blights.put(type, severity);
        if (shouldSync) this.sync();
    }

    @Override
    public void clearSeverities(boolean shouldSync) {
        blights.clear();
        if (shouldSync) this.sync();
    }

    @Override
    public void tick() {
        World world = provider.getWorld();
        if (world == null) return;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains(NbtKeys.ENTITY_BLIGHTS)) {
            clearSeverities(false);
            NbtCompound nbt = tag.getCompound(NbtKeys.ENTITY_BLIGHTS);
            for (String key : nbt.getKeys()) {
                BlightType type = BlightType.fromString(key);
                Severity severity = Severity.fromString(nbt.getString(key));
                setSeverity(type, severity, false);
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
        tag.put(NbtKeys.ENTITY_BLIGHTS, nbt);
    }
}
