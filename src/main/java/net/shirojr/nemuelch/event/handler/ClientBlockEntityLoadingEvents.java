package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class ClientBlockEntityLoadingEvents implements ClientBlockEntityEvents.Load, ClientBlockEntityEvents.Unload {
    public static final Set<AdvancedFogBlockEntity> LOADED_ADVANCED_FOG_BLOCKS = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void onLoad(BlockEntity blockEntity, ClientWorld world) {
        if (blockEntity instanceof AdvancedFogBlockEntity fogBlockEntity) {
            LOADED_ADVANCED_FOG_BLOCKS.add(fogBlockEntity);
        }
    }

    @Override
    public void onUnload(BlockEntity blockEntity, ClientWorld world) {
        if (blockEntity instanceof AdvancedFogBlockEntity fogBlockEntity) {
            LOADED_ADVANCED_FOG_BLOCKS.remove(fogBlockEntity);
        }
    }
}
