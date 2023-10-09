package net.shirojr.nemuelch.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;

import java.util.ArrayList;
import java.util.List;

public class PersistentWorldData extends PersistentState {
    public List<Integer> usedSleepEventEntries = new ArrayList<>();
    public static final String SLEEP_EVENT_NBT_KEY = "usedSleepEventEntries";

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putIntArray(SLEEP_EVENT_NBT_KEY, usedSleepEventEntries);
        return nbt;
    }

    public static PersistentWorldData createFromNbt(NbtCompound nbt) {
        PersistentWorldData worldData = new PersistentWorldData();
        worldData.usedSleepEventEntries.clear();
        for (int entry : nbt.getIntArray(SLEEP_EVENT_NBT_KEY)) {
            worldData.usedSleepEventEntries.add(entry);
        }
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
