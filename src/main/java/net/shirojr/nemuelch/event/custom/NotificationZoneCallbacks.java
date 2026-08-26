package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.NotificationZone;

public class NotificationZoneCallbacks {
    public static Event<EnteredZone> ENTERED_ZONE = EventFactory.createArrayBacked(EnteredZone.class,
            listeners -> (component, zone, entity) -> {
                for (EnteredZone listener : listeners) {
                    listener.onZoneEntered(component, zone, entity);
                }
            }
    );

    public static Event<LeftZone> LEFT_ZONE = EventFactory.createArrayBacked(LeftZone.class,
            listeners -> (component, zone, entity) -> {
                for (LeftZone listener : listeners) {
                    listener.onZoneLeft(component, zone, entity);
                }
            }
    );

    @FunctionalInterface
    public interface EnteredZone {
        void onZoneEntered(NotificationZoneComponent component, NotificationZone zone, LivingEntity entity);
    }

    @FunctionalInterface
    public interface LeftZone {
        void onZoneLeft(NotificationZoneComponent component, NotificationZone zone, LivingEntity entity);
    }
}
