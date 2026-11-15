package net.shirojr.nemuelch.util.duck;

import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.init.NeMuelchGameRules;

public interface ParticleDelayHolder {
    int nemuelch$getDelay();

    void nemuelch$setDelay(int delay);

    default boolean displaysParticles() {
        return this.nemuelch$getDelay() <= 0;
    }

    default void restartDelay(ServerWorld serverWorld) {
        this.nemuelch$setDelay(serverWorld.getGameRules().getInt(NeMuelchGameRules.ALCHEMISTS_HINT_DELAY_SECONDS));
    }
}
