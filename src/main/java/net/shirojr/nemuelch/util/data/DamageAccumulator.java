package net.shirojr.nemuelch.util.data;

import net.minecraft.nbt.NbtCompound;

import java.util.*;

public class DamageAccumulator {
    private final HashSet<Callback> callbacks;
    private final LinkedList<DamageEntry> damages;

    public DamageAccumulator(Callback callback) {
        this(callback, new LinkedList<>());
    }

    public DamageAccumulator(Callback callback, LinkedList<DamageEntry> damages) {
        this.callbacks = new HashSet<>(Set.of(callback));
        this.damages = damages;
    }

    @SuppressWarnings("unused")
    public void addCallback(Callback callback) {
        this.callbacks.add(callback);
    }

    public void addDamage(DamageEntry damage) {
        this.damages.add(damage);
    }

    public DamageEntry getNewestDamage() {
        return this.damages.getLast();
    }

    public float getDamagePerTick(float currentAge, float tickWindow) {
        float total = 0f;
        for (DamageEntry entry : damages) {
            if (currentAge - entry.age() <= tickWindow) {
                total += entry.damage();
            }
        }
        return total / tickWindow;
    }

    public float getDamagePerSecond(float currentAge, float tickWindow) {
        return getDamagePerTick(currentAge, tickWindow) * 20;
    }

    public float getAverageDamage() {
        if (damages.isEmpty()) return 0;
        float total = 0f;
        for (DamageEntry entry : damages) {
            total += entry.damage();
        }
        return total / damages.size();
    }

    public float getTotalDamage() {
        float sum = 0;
        for (DamageEntry entry : this.damages) {
            sum += entry.damage;
        }
        return sum;
    }

    public int getHits() {
        return this.damages.size();
    }

    public boolean isEmpty() {
        return this.damages.isEmpty();
    }

    public void clear() {
        List<DamageEntry> oldEntries = Collections.unmodifiableList(this.damages);
        this.damages.clear();
        this.callbacks.forEach(callback -> callback.onDamageCleared(oldEntries));
    }

    public record DamageEntry(float damage, float angleInRad, int age) {
        public static DamageEntry fromNbt(NbtCompound nbt) {
            return new DamageEntry(nbt.getFloat("damage"), nbt.getFloat("angle"), nbt.getInt("age"));
        }

        public void toNbt(NbtCompound nbt) {
            nbt.putFloat("damage", this.damage);
            nbt.putFloat("angle", this.angleInRad);
            nbt.putInt("age", this.age);
        }
    }

    public interface Callback {
        default void onDamageAdded(List<DamageEntry> newEntries) {
        }

        default void onDamageCleared(List<DamageEntry> oldEntries) {
        }
    }
}
