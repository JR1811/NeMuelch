package net.shirojr.nemuelch.event;

import net.shirojr.nemuelch.event.custom.CommandRegistrationEvents;
import net.shirojr.nemuelch.event.custom.KeyBindEvents;
import net.shirojr.nemuelch.event.custom.SleepEvents;

public class NeMuelchEvents {
    public static void registerEvents() {
        SleepEvents.register();
        CommandRegistrationEvents.register();
    }
    public static void registerClientEvents() {
        KeyBindEvents.register();
    }
}
