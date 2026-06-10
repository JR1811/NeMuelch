package net.shirojr.nemuelch.mixin.access;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerAccess {
    @Accessor("ticketManager")
    ChunkTicketManager getTicketManager();
}
