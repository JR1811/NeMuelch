package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.camera.DisplacementSequence;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class DisplacementSequenceRegistryComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("displacement_sequence_registry");

    private final HashMap<Identifier, DisplacementSequence> entries;
    private final Scoreboard scoreboard;

    public DisplacementSequenceRegistryComponent(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.entries = new HashMap<>();
    }

    public static DisplacementSequenceRegistryComponent get(Scoreboard scoreboard) {
        return NeMuelchComponents.DISPLACEMENT_SEQUENCES.get(scoreboard);
    }

    public List<Identifier> getEntryKeys() {
        List<Identifier> keys = new ArrayList<>(this.entries.keySet());
        Collections.sort(keys);
        return Collections.unmodifiableList(keys);
    }

    public Map<Identifier, DisplacementSequence> getEntries() {
        return Collections.unmodifiableMap(this.entries);
    }

    public void modifyEntries(boolean sync, Consumer<HashMap<Identifier, DisplacementSequence>> entries) {
        entries.accept(this.entries);
        if (sync) this.sync();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtCompound entriesNbt = tag.getCompound("entries");

        this.entries.clear();
        for (String key : entriesNbt.getKeys()) {
            NbtCompound entryNbt = (NbtCompound) entriesNbt.get(key);
            Identifier identifier = Identifier.tryParse(key);
            if (entryNbt == null || identifier == null) {
                String warning = "DisplacementSequence \"%s\" not readable. Skipping Entry".formatted(key);
                NeMuelch.LOGGER.warn(warning);
                continue;
            }
            this.entries.put(identifier, DisplacementSequence.fromNbt(entryNbt, identifier));
        }

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtCompound entriesNbt = new NbtCompound();
        for (var entry : entries.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            DisplacementSequence.toNbt(entryNbt, entry.getKey(), entry.getValue());
            entriesNbt.put(entry.getKey().toString(), entryNbt);
        }
        tag.put("entries", entriesNbt);
    }

    public void sync() {
        NeMuelchComponents.DISPLACEMENT_SEQUENCES.sync(this.scoreboard);
    }
}
