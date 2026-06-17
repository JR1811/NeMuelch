package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.World;

public class DayStateCallbacks {
    public static Event<DayStateCallbacks.DayRisingAction> ON_DAY_START = EventFactory.createArrayBacked(DayStateCallbacks.DayRisingAction.class,
            listeners -> world -> {
                for (DayStateCallbacks.DayRisingAction listener : listeners) {
                    listener.onRisingEdgeDay(world);
                }
            }
    );

    public static Event<DayStateCallbacks.DayFallingAction> ON_DAY_END = EventFactory.createArrayBacked(DayStateCallbacks.DayFallingAction.class,
            listeners -> world -> {
                for (DayStateCallbacks.DayFallingAction listener : listeners) {
                    listener.onFallingEdgeDay(world);
                }
            }
    );

    public static Event<DayStateCallbacks.NightRisingAction> ON_NIGHT_START = EventFactory.createArrayBacked(DayStateCallbacks.NightRisingAction.class,
            listeners -> world -> {
                for (DayStateCallbacks.NightRisingAction listener : listeners) {
                    listener.onRisingEdgeNight(world);
                }
            }
    );

    public static Event<DayStateCallbacks.NightFallingAction> ON_NIGHT_END = EventFactory.createArrayBacked(DayStateCallbacks.NightFallingAction.class,
            listeners -> world -> {
                for (DayStateCallbacks.NightFallingAction listener : listeners) {
                    listener.onFallingEdgeNight(world);
                }
            }
    );

    @FunctionalInterface
    public interface DayRisingAction {
        void onRisingEdgeDay(World world);
    }

    @FunctionalInterface
    public interface DayFallingAction {
        void onFallingEdgeDay(World world);
    }

    @FunctionalInterface
    public interface NightRisingAction {
        void onRisingEdgeNight(World world);
    }

    @FunctionalInterface
    public interface NightFallingAction {
        void onFallingEdgeNight(World world);
    }
}
