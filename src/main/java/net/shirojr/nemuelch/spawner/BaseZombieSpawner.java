package net.shirojr.nemuelch.spawner;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.Spawner;

public class BaseZombieSpawner implements Spawner {
    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        return 0;
    }
}
