package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.shirojr.nemuelch.event.custom.*;

public class NeMuelchEvents {
    public static void initializeCommon() {
        CommandRegistrationEvents.registerCommon();
        PlayerJoinEvents.register();
        ServerMiscEvents.initialize();
        AttackCallbacks attackCallbacks = new AttackCallbacks();
        AttackEntityCallback.EVENT.register(attackCallbacks);
        AttackBlockCallback.EVENT.register(attackCallbacks);
        SleepEvents sleepEvents = new SleepEvents();
        EntitySleepEvents.START_SLEEPING.register(sleepEvents);
        EntitySleepEvents.STOP_SLEEPING.register(sleepEvents);
        UseEvents useEvents = new UseEvents();
        UseEntityCallback.EVENT.register(useEvents);
        UseBlockCallback.EVENT.register(useEvents);
    }

    public static void initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(new KeyBindEvents());
        RenderEvents.register();
    }
}
