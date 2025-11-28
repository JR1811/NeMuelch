package net.shirojr.nemuelch.camera;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.implementation.DisplacementSequenceRegistryComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class DisplacementSequence {
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    private int elapsed;

    public DisplacementSequence() {
        this(new ArrayList<>());
    }

    public DisplacementSequence(List<Entry> entries) {
        this(entries, 0);
    }

    public DisplacementSequence(List<Entry> entries, int elapsed) {
        this.entries.addAll(entries);
        this.elapsed = elapsed;
    }

    public static DisplacementSequence fromRegistry(Identifier identifier, Scoreboard scoreboard) {
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);
        if (!component.getEntryKeys().contains(identifier)) {
            IllegalArgumentException e = new IllegalArgumentException("CCA component registry didn't contain key for : " + identifier.toString());
            NeMuelch.LOGGER.error("No such Camera Displacement Sequence key", e);
            throw e;
        }
        return component.getEntries().get(identifier).copy();
    }

    public void addEntry(Displacement displacement, int activeDuration, int holdDuration, Easing easing) {
        if (!entries.isEmpty()) {
            Entry lastEntry = entries.peekLast();
            this.entries.offer(new Entry(activeDuration, holdDuration, lastEntry, displacement, easing));
        } else {
            this.entries.offer(new Entry(activeDuration, holdDuration, displacement, easing));
        }
    }

    public List<Entry> getEntries() {
        return List.copyOf(entries);
    }

    @Nullable
    public DisplacementSequence.Entry getActive() {
        return entries.peek();
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = Math.max(0, elapsed);
    }

    @SuppressWarnings("unused")
    public int getDurationLeft() {
        int result = 0;
        for (Entry entry : this.entries) {
            result = result + entry.getFullDuration();
        }
        result -= getElapsed();
        return Math.max(0, result);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isActive() {
        return !entries.isEmpty();
    }

    public void clear() {
        this.entries.clear();
        setElapsed(0);
    }

    public DisplacementSequence copy() {
        return new DisplacementSequence(new ArrayList<>(this.entries), this.elapsed);
    }

    public void tick() {
        if (getActive() == null) {
            if (getElapsed() > 0) setElapsed(0);
            return;
        }

        if (getElapsed() >= getActive().getFullDuration()) {
            this.entries.pollFirst();
            setElapsed(0);
        } else {
            setElapsed(getElapsed() + 1);
        }
    }

    public Displacement getInterpolatedDisplacement(float tickDelta) {
        Entry activeEntry = getActive();
        if (activeEntry == null) return Displacement.DEFAULT;

        float progress = (getElapsed() + tickDelta) / (float) activeEntry.activeDuration;
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);

        return activeEntry.easing.interpolate(progress, activeEntry.startDisplacement, activeEntry.endDisplacement);
    }

    public static void toNbt(NbtCompound nbt, Identifier key, DisplacementSequence sequence) {
        NbtCompound sequenceNbt = new NbtCompound();

        NbtList entriesNbt = new NbtList();
        for (Entry entry : sequence.entries) {
            NbtCompound entryNbt = new NbtCompound();
            Entry.toNbt(entryNbt, entry);
            entriesNbt.add(entryNbt);
        }
        sequenceNbt.put("entries", entriesNbt);
        sequenceNbt.putInt("elapsed", sequence.elapsed);

        nbt.put(key.toString(), sequenceNbt);
    }

    public static DisplacementSequence fromNbt(NbtCompound nbt, Identifier key) {
        List<Entry> entries = new ArrayList<>();

        NbtCompound sequenceNbt = nbt.getCompound(key.toString());
        NbtList entriesNbt = sequenceNbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for (NbtElement nbtElement : entriesNbt) {
            NbtCompound entryNbt = (NbtCompound) nbtElement;
            entries.add(Entry.fromNbt(entryNbt));
        }

        int elapsed = sequenceNbt.getInt("elapsed");
        return new DisplacementSequence(entries, elapsed);
    }


    public record Entry(int activeDuration, int holdEndFrameDuration, Displacement startDisplacement,
                        Displacement endDisplacement, Easing easing) {
        public Entry(int activeDuration, int holdEndFrameDuration, Entry previous, Displacement targetDisplacement, Easing easing) {
            this(activeDuration, holdEndFrameDuration, previous.endDisplacement, targetDisplacement, easing);
        }

        public Entry(int activeDuration, int holdEndFrameDuration, Displacement endDisplacement, Easing easing) {
            this(activeDuration, holdEndFrameDuration, Displacement.DEFAULT, endDisplacement, easing);
        }

        public int getFullDuration() {
            return activeDuration + holdEndFrameDuration;
        }

        public static void toNbt(NbtCompound nbt, Entry entry) {
            NbtCompound entryNbt = new NbtCompound();

            entryNbt.putInt("activeDuration", entry.activeDuration);
            entryNbt.putInt("holdDuration", entry.holdEndFrameDuration);
            Displacement.toNbt(entryNbt, "start", entry.startDisplacement);
            Displacement.toNbt(entryNbt, "end", entry.endDisplacement);
            entryNbt.putInt("easingIndex", entry.easing.ordinal());

            nbt.put("displacementEntry", entryNbt);
        }

        public static Entry fromNbt(NbtCompound nbt) {
            NbtCompound entryNbt = nbt.getCompound("displacementEntry");

            int activeDuration = entryNbt.getInt("activeDuration");
            int holdDuration = entryNbt.getInt("activeDuration");

            Displacement startDisplacement = Displacement.fromNbt(entryNbt, "start");
            Displacement endDisplacement = Displacement.fromNbt(entryNbt, "end");

            Easing easing = Easing.values()[entryNbt.getInt("easingIndex")];

            return new Entry(activeDuration, holdDuration, startDisplacement, endDisplacement, easing);
        }
    }
}
