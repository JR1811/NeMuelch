package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.shirojr.nemuelch.event.custom.*;
import net.shirojr.nemuelch.render.BlightDebugRenderer;

public class NeMuelchEvents {
    public static void initializeCommon() {
        AttackCallbacks attackCallbacks = new AttackCallbacks();
        SleepEvents sleepEvents = new SleepEvents();
        UseEvents useEvents = new UseEvents();
        ItemEvents itemEvents = new ItemEvents();
        LootEvents lootEvents = new LootEvents();

        CommandRegistrationEvents.registerCommon();
        PlayerJoinEvents.register();
        ServerMiscEvents.initialize();
        AttackEntityCallback.EVENT.register(attackCallbacks);
        AttackBlockCallback.EVENT.register(attackCallbacks);
        EntitySleepEvents.START_SLEEPING.register(sleepEvents);
        EntitySleepEvents.STOP_SLEEPING.register(sleepEvents);
        UseEntityCallback.EVENT.register(useEvents);
        UseBlockCallback.EVENT.register(useEvents);
        UseItemCallback.EVENT.register(itemEvents);
        LootTableEvents.MODIFY.register(lootEvents);
    }

    public static void initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(new KeyBindEvents());
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(new BlightDebugRenderer());
        RenderEvents.register();
    }
}
