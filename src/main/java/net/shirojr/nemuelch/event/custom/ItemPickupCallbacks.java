package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;

public class ItemPickupCallbacks {
    public static Event<EntityPickup> ON_ENTITY_PICKED_UP_ITEM = EventFactory.createArrayBacked(EntityPickup.class,
            listeners -> (entity, itemEntity) -> {
                for (EntityPickup listener : listeners) {
                    listener.onEntityPickedUpItem(entity, itemEntity);
                }
            }
    );

    @FunctionalInterface
    public interface EntityPickup {
        void onEntityPickedUpItem(LivingEntity entity, ItemEntity itemEntity);
    }
}
