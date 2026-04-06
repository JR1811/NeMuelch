package net.shirojr.nemuelch.util.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.LinkedList;

@SuppressWarnings("unused")
public class DamageAccumulator {
    private final LinkedList<DamageEntry> damages;

    public DamageAccumulator() {
        this(new LinkedList<>());
    }

    public DamageAccumulator(LinkedList<DamageEntry> damages) {
        this.damages = damages;
    }

    public LinkedList<DamageEntry> getDamages() {
        return damages;
    }

    public DamageEntry getNewestDamage() {
        return this.damages.getLast();
    }

    public DamageEntry getOldestEntry() {
        return this.damages.getFirst();
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
        float total = 0f;
        for (DamageEntry entry : damages) {
            total += entry.damage();
        }
        return total / damages.size();
    }

    public boolean isEmpty() {
        return this.damages.isEmpty();
    }

    public static DamageAccumulator fromNbt(NbtCompound nbt) {
        LinkedList<DamageEntry> entries = new LinkedList<>();
        NbtList nbtList = nbt.getList("damages", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++) {
            entries.add(DamageEntry.fromNbt(nbtList.getCompound(i)));
        }
        return new DamageAccumulator(entries);
    }

    public void toNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (DamageEntry damage : this.damages) {
            NbtCompound damageNbt = new NbtCompound();
            damage.toNbt(damageNbt);
            nbtList.add(damageNbt);
        }
        nbt.put("damages", nbtList);
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
}
