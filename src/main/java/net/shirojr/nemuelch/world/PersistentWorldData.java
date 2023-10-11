package net.shirojr.nemuelch.world;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;

import java.util.*;
import java.util.stream.Collectors;

public class PersistentWorldData extends PersistentState {
    public List<SleepEventHelper.SleepEventDataEntry> usedSleepEventEntries = new ArrayList<>();
    public static final String SLEEP_EVENT_NBT_KEY = NeMuelch.MOD_ID + ".sleepEvent";
    public static final String ENTRY_SLEEP_EVENT_NBT_KEY = "sleepEventEntry";
    public static final String PLAYER_SLEEP_EVENT_NBT_KEY = "sleepEventEntryFromPlayer";


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (var entry : usedSleepEventEntries) {
            NbtCompound sleepEntriesNbtCompound = new NbtCompound();
            sleepEntriesNbtCompound.putInt(ENTRY_SLEEP_EVENT_NBT_KEY, entry.sleepEvent());
            sleepEntriesNbtCompound.putUuid(PLAYER_SLEEP_EVENT_NBT_KEY, entry.playerUuid());

            nbtList.add(sleepEntriesNbtCompound);
        }
        nbt.put(SLEEP_EVENT_NBT_KEY, nbtList);
        return nbt;
    }

    public static PersistentWorldData createFromNbt(NbtCompound nbt) {
        PersistentWorldData worldData = new PersistentWorldData();
        NbtList sleepEntriesNbtList = nbt.getList(SLEEP_EVENT_NBT_KEY, NbtElement.COMPOUND_TYPE);

        sleepEntriesNbtList.forEach(nbtElement -> {
            if (!(nbtElement instanceof NbtCompound nbtCompound)) return;

            int entry = nbtCompound.getInt(ENTRY_SLEEP_EVENT_NBT_KEY);
            UUID uuid = nbtCompound.getUuid(PLAYER_SLEEP_EVENT_NBT_KEY);

            SleepEventHelper.SleepEventDataEntry sleepEventDataEntry = new SleepEventHelper.SleepEventDataEntry(uuid, entry);
            worldData.usedSleepEventEntries.add(sleepEventDataEntry);
        });

        return worldData;
    }

    @SuppressWarnings("DataFlowIssue")
    public static PersistentWorldData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        PersistentWorldData state = persistentStateManager.getOrCreate(PersistentWorldData::createFromNbt, PersistentWorldData::new, NeMuelch.MOD_ID);
        state.markDirty();
        return state;
    }

}
