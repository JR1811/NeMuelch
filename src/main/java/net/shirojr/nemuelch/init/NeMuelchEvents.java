package net.shirojr.nemuelch.init;

import net.shirojr.nemuelch.event.custom.*;

public class NeMuelchEvents {
    public static void initializeCommon() {
        SleepEvents.register();
        CommandRegistrationEvents.register();
        ServerConnectionEvents.register();
        EntityLootEvents.register();
    }
    public static void initializeClient() {
        KeyBindEvents.register();
        RenderEvents.register();
    }
}
