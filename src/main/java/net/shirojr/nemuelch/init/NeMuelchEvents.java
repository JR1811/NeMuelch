package net.shirojr.nemuelch.init;

import net.shirojr.nemuelch.event.custom.*;

public class NeMuelchEvents {
    public static void initializeCommon() {
        SleepEvents.register();
        CommandRegistrationEvents.registerCommon();
        PlayerJoinEvents.register();
        ServerMiscEvents.initialize();
    }

    public static void initializeClient() {
        KeyBindEvents.register();
        RenderEvents.register();
    }
}
