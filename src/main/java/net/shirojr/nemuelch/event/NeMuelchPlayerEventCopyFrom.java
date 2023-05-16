package net.shirojr.nemuelch.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;
import net.shirojr.nemuelch.util.BodyParts;

/**
 * on death of player
 */
public class NeMuelchPlayerEventCopyFrom implements ServerPlayerEvents.CopyFrom{
    /**
     * "Lambda-ception" - JoeMama 2023
     * @param oldPlayer the old player
     * @param newPlayer the new player
     * @param alive whether the old player is still alive
     */
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        ((IBodyPartSaver)oldPlayer).editPersistentData(nbtCompound ->
            ((IBodyPartSaver)newPlayer).editPersistentData(player -> {
                for (var entry : BodyParts.values()) {
                    if (nbtCompound.contains(entry.getBodyPartName())) {
                        player.putString(entry.getBodyPartName(),
                                nbtCompound.getString(entry.getBodyPartName()));
                    }
                }
                return true;
            })
        );
    }
}