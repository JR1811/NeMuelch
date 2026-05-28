package net.shirojr.nemuelch.util.duck;

import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.init.NemuelchGameRules;

public interface Generation {
    int nemuelch$getGeneration();

    void nemuelch$setGeneration(int generation);

    static int getMaxGeneration(ServerWorld world) {
        return world.getGameRules().getInt(NemuelchGameRules.OCCASION_REINFORCEMENTS_MAX_GENERATION);
    }
}
