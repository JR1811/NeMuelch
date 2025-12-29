package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class OccasionsWorldComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("occasions");
    public static final String SCHEDULED_OCCASIONS_NBT_KEY = "ScheduledOccasions";
    private final World provider;

    private final List<OccasionEntry> scheduledOccasions;

    public OccasionsWorldComponent(World provider) {
        this.provider = provider;
        this.scheduledOccasions = new ArrayList<>();
    }

    public static OccasionsWorldComponent get(World world) {
        return NeMuelchComponents.OCCASION.get(world);
    }

    public World getProvider() {
        return provider;
    }

    public boolean isEmpty() {
        return this.scheduledOccasions.isEmpty();
    }

    public void modifyScheduledOccasions(Consumer<List<OccasionEntry>> occasions, boolean shouldSync) {
        occasions.accept(this.scheduledOccasions);
        if (shouldSync) this.sync();
    }

    /**
     * This method...
     * <ul>
     *     <li>schedules a new Occasion</li>
     *     <li> {@link OccasionEntry#onFinish(World) finishes} and cleans up old occasions, which were excluded by the new occasion</li>
     * </ul>
     *
     * @param occasion new Occasion entry
     * @return old occasions which were excluded by the new one
     */
    public List<OccasionEntry> addOccasion(OccasionEntry occasion) {
        List<OccasionEntry> toBeExcluded = new ArrayList<>();
        modifyScheduledOccasions(occasionEntries -> {
            for (OccasionEntry oldOccasionsEntry : occasionEntries) {
                if (!occasion.intersects(oldOccasionsEntry)) continue;

                if (occasion.getType().excludeOther().test(oldOccasionsEntry.getType())) {
                    toBeExcluded.add(oldOccasionsEntry);
                    if (oldOccasionsEntry.getState(provider.getTime()) == OccasionState.ACTIVE) {
                        oldOccasionsEntry.onFinish(provider);
                    }
                }
            }
        }, true);

        this.scheduledOccasions.removeAll(toBeExcluded);
        this.scheduledOccasions.add(occasion);
        this.sync();
        return toBeExcluded;
    }

    public List<OccasionEntry> getUnsyncedScheduledOccasions() {
        return Collections.unmodifiableList(this.scheduledOccasions);
    }

    public List<OccasionEntry> getUnsyncedActiveOccasions(long time) {
        List<OccasionEntry> result = new ArrayList<>();
        for (OccasionEntry entry : this.scheduledOccasions) {
            if (entry.getState(time) == OccasionState.ACTIVE) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<OccasionEntry> getUnsyncedActiveOccasions() {
        return getUnsyncedActiveOccasions(provider.getTime());
    }

    private void clearFinishedEntries() {
        List<OccasionEntry> finishedEvents = new ArrayList<>();
        if (this.scheduledOccasions.isEmpty()) return;
        for (OccasionEntry activeOccasionEntry : this.scheduledOccasions) {
            if (activeOccasionEntry.getState(provider.getTime()) == OccasionState.FINISHED) {
                finishedEvents.add(activeOccasionEntry);
                activeOccasionEntry.onFinish(provider);
            }
        }
        if (finishedEvents.isEmpty()) return;
        this.scheduledOccasions.removeAll(finishedEvents);
        this.sync();
    }

    @Override
    public void serverTick() {
        clearFinishedEntries();
        scheduledOccasions.forEach(entry -> entry.tick(provider));
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains(SCHEDULED_OCCASIONS_NBT_KEY)) {
            this.scheduledOccasions.clear();
            NbtList nbtList = nbt.getList(SCHEDULED_OCCASIONS_NBT_KEY, NbtElement.COMPOUND_TYPE);
            for (NbtElement listElement : nbtList) {
                NbtCompound entryNbt = (NbtCompound) listElement;
                this.scheduledOccasions.add(OccasionEntry.fromNbt(entryNbt));
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (OccasionEntry entry : this.scheduledOccasions) {
            NbtCompound entryNbt = new NbtCompound();
            entry.toNbt(entryNbt);
            nbtList.add(entryNbt);
        }
        nbt.put(SCHEDULED_OCCASIONS_NBT_KEY, nbtList);
    }

    public void sync() {
        if (!(provider instanceof ServerWorld)) return;
        NeMuelchComponents.OCCASION.sync(provider);
    }
}
