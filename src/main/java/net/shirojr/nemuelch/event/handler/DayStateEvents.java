package net.shirojr.nemuelch.event.handler;

import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.event.custom.DayStateCallbacks;
import net.shirojr.nemuelch.occasion.OccasionEntry;

public class DayStateEvents implements DayStateCallbacks.DayRisingAction, DayStateCallbacks.DayFallingAction,
        DayStateCallbacks.NightRisingAction, DayStateCallbacks.NightFallingAction {

    @Override
    public void onRisingEdgeDay(World world) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            entry.getType().onDayStart(world, entry);
        }
    }

    @Override
    public void onFallingEdgeDay(World world) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            entry.getType().onDayEnd(world, entry);
        }
    }

    @Override
    public void onRisingEdgeNight(World world) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            entry.getType().onNightStart(world, entry);
        }
    }

    @Override
    public void onFallingEdgeNight(World world) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            entry.getType().onNightEnd(world, entry);
        }
    }
}
