package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.FoodModificationEntry;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.function.UnaryOperator;

//TODO:
//  - add counter
//  - user render particles if they consumed that type of modified food a lot already

public class FoodModificationComponent implements Component, ServerTickingComponent, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("food_modification");
    private final LivingEntity entity;

    private final Object2IntOpenHashMap<FoodModificationEntry> activeEntries = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<StatusEffect> consumedCounter = new Object2IntOpenHashMap<>();

    public FoodModificationComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public static FoodModificationComponent get(LivingEntity entity) {
        return NeMuelchComponents.FOOD_MODIFICATION.get(entity);
    }

    public void addEntry(@Nullable FoodModificationEntry entry, int delay) {
        if (entry == null) return;
        this.activeEntries.put(entry, Math.max(0, delay));
        this.sync();
    }

    public void setConsumedCount(StatusEffect effect, UnaryOperator<Integer> modifier) {
        if (modifier == null) {
            this.consumedCounter.removeInt(effect);
            return;
        }
        int oldCount = this.consumedCounter.getOrDefault(effect, 0);
        this.consumedCounter.put(effect, (int) modifier.apply(oldCount));
        this.sync();
    }

    public void clear() {
        this.activeEntries.clear();
        this.sync();
    }

    public OptionalInt getColor() {
        if (this.activeEntries.isEmpty()) return OptionalInt.empty();
        return OptionalInt.of(FoodModificationEntry.getColor(this.activeEntries.keySet()));
    }

    @Override
    public void serverTick() {
        if (this.activeEntries.isEmpty() || !this.entity.isAlive()) return;
        boolean changed = false;
        ObjectIterator<Object2IntMap.Entry<FoodModificationEntry>> iterator = this.activeEntries.object2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Object2IntMap.Entry<FoodModificationEntry> entry = iterator.next();
            int remaining = entry.getIntValue() - 1;
            if (remaining <= 0) {
                entry.getKey().apply(this.entity);
                entry.getKey().getPotion().getEffects().forEach(instance -> this.consumedCounter.addTo(instance.getEffectType(), 1));
                iterator.remove();
                changed = true;
            } else {
                entry.setValue(remaining);
            }
        }
        if (changed) {
            this.sync();
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.activeEntries.clear();
        if (nbt.contains(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION)) {
            NbtList entriesNbt = nbt.getList(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < entriesNbt.size(); i++) {
                NbtCompound entryNbt = entriesNbt.getCompound(i);
                FoodModificationEntry entry = FoodModificationEntry.fromNbt(entryNbt.getCompound(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY));
                if (entry == null) continue;
                int delay = entryNbt.getInt(NeMuelchNbtKeys.DELAY);
                this.activeEntries.put(entry, delay);
            }
        }

        this.consumedCounter.clear();
        if (nbt.contains(NeMuelchNbtKeys.CONSUMED_COUNTER)) {
            NbtCompound entriesNbt = nbt.getCompound(NeMuelchNbtKeys.CONSUMED_COUNTER);
            for (String entryKey : entriesNbt.getKeys()) {
                Identifier id = Identifier.tryParse(entryKey);
                if (id == null) continue;
                StatusEffect effect = Registries.STATUS_EFFECT.get(id);
                if (effect == null) continue;
                int amount = entriesNbt.getInt(entryKey);
                this.consumedCounter.put(effect, amount);
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        if (!this.activeEntries.isEmpty()) {
            NbtList activeEntriesNbt = new NbtList();
            for (var entry : this.activeEntries.object2IntEntrySet()) {
                NbtCompound entryNbt = new NbtCompound();
                entryNbt.put(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY, entry.getKey().toNbt());
                entryNbt.putInt(NeMuelchNbtKeys.DELAY, entry.getIntValue());
                activeEntriesNbt.add(entryNbt);
            }
            nbt.put(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION, activeEntriesNbt);
        }
        if (!this.consumedCounter.isEmpty()) {
            NbtCompound effectCounterNbt = new NbtCompound();
            for (var entry : this.consumedCounter.object2IntEntrySet()) {
                Identifier id = Registries.STATUS_EFFECT.getId(entry.getKey());
                if (id == null) continue;
                effectCounterNbt.putInt(id.toString(), entry.getIntValue());
            }
            nbt.put(NeMuelchNbtKeys.CONSUMED_COUNTER, effectCounterNbt);
        }
    }

    public void sync() {
        if (!(this.entity.getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.FOOD_MODIFICATION.sync(this.entity);
    }
}
