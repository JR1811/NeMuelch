package net.shirojr.nemuelch.event.handler;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.event.custom.ItemPickupCallbacks;

public class PickedUpItemEvents implements ItemPickupCallbacks.EntityPickup {
    @Override
    public void onEntityPickedUpItem(LivingEntity entity, ItemEntity itemEntity) {
        if (entity instanceof ServerPlayerEntity player) {
            MonsterComponent.get(player).getActiveType().ifPresent(type -> {
                type.onPickedUpItem(player, itemEntity);
            });
        }
    }
}
