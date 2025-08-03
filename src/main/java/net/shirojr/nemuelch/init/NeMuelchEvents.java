package net.shirojr.nemuelch.init;

import net.shirojr.nemuelch.event.custom.CommandRegistrationEvents;
import net.shirojr.nemuelch.event.custom.KeyBindEvents;
import net.shirojr.nemuelch.event.custom.RenderEvents;
import net.shirojr.nemuelch.event.custom.SleepEvents;

public class NeMuelchEvents {
    public static void initializeCommon() {
        SleepEvents.register();
        CommandRegistrationEvents.registerCommon();
    }

    public static void initializeClient() {
        KeyBindEvents.register();
        RenderEvents.register();
    }
}
