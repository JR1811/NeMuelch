package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public class AcidCallbacks {
    public static Event<DirectContact> IS_DIRECT_CONTACT_PROTECTED = EventFactory.createArrayBacked(DirectContact.class,
            listeners -> entity -> {
                for (DirectContact listener : listeners) {
                    if (listener.isContactProtected(entity)) return true;
                }
                return false;
            }
    );

    public static Event<AtmosphereContact> IS_ATMOSPHERE_PROTECTED = EventFactory.createArrayBacked(AtmosphereContact.class,
            listeners -> entity -> {
                for (AtmosphereContact listener : listeners) {
                    if (listener.isAtmosphereProtected(entity)) return true;
                }
                return false;
            }
    );

    @FunctionalInterface
    public interface DirectContact {
        boolean isContactProtected(Entity entity);
    }

    @FunctionalInterface
    public interface AtmosphereContact {
        boolean isAtmosphereProtected(Entity entity);
    }
}
