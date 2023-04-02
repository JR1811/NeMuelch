package net.shirojr.nemuelch.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;
import net.shirojr.nemuelch.util.BodyParts;

/**
 * on death of player
 */
public class NeMuelchPlayerEventCopyFrom implements ServerPlayerEvents.CopyFrom{
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        for (var entry : BodyParts.values()) {
            IBodyPartSaver original = (IBodyPartSaver) oldPlayer;
            IBodyPartSaver player = (IBodyPartSaver) newPlayer;

            if (original.getPersistentData().contains(entry.getBodyPartName())) {
                player.getPersistentData().putString(entry.getBodyPartName(),
                        original.getPersistentData().getString(entry.getBodyPartName()));
            }
        }
    }
}
