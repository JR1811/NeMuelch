package net.shirojr.nemuelch.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;

import java.util.ArrayList;
import java.util.List;

public class PersistentWorldData extends PersistentState {
    public List<SleepEventHelper.SleepEventDataEntry> usedSleepEventEntries = new ArrayList<>();
    public static final String SLEEP_EVENT_NBT_KEY = NeMuelch.MOD_ID + ".sleepEventIndex";
    public static final String ENTRY_SLEEP_EVENT_NBT_KEY = "sleepEventEntry";
    public static final String PLAYER_NAME_SLEEP_EVENT_NBT_KEY = "sleepEventEntryFromPlayer";


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (var entry : usedSleepEventEntries) {
            NbtCompound sleepEntriesNbtCompound = new NbtCompound();
            sleepEntriesNbtCompound.putInt(ENTRY_SLEEP_EVENT_NBT_KEY, entry.sleepEventIndex());
            sleepEntriesNbtCompound.putString(PLAYER_NAME_SLEEP_EVENT_NBT_KEY, entry.playerName());

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
            String playerName = nbtCompound.getString(PLAYER_NAME_SLEEP_EVENT_NBT_KEY);

            SleepEventHelper.SleepEventDataEntry sleepEventDataEntry = new SleepEventHelper.SleepEventDataEntry(playerName, entry);
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
