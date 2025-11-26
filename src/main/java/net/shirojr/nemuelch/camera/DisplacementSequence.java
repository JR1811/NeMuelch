package net.shirojr.nemuelch.camera;

import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;

public class DisplacementSequence {
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    private int elapsed;

    public DisplacementSequence(List<Entry> entries) {
        this.entries.addAll(entries);
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
    }
}
