package net.shirojr.nemuelch.event;

import net.shirojr.nemuelch.event.custom.*;

public class NeMuelchEvents {
    public static void registerEvents() {
        SleepEvents.register();
        CommandRegistrationEvents.register();
        ServerConnectionEvents.register();
    }
    public static void registerClientEvents() {
        KeyBindEvents.register();
        RenderEvents.register();
    }
}
