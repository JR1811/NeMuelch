package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.shirojr.nemuelch.event.custom.AcidCallbacks;
import net.shirojr.nemuelch.event.custom.BlockStateCallbacks;
import net.shirojr.nemuelch.event.custom.DayStateCallbacks;
import net.shirojr.nemuelch.event.custom.ItemPickupCallbacks;
import net.shirojr.nemuelch.event.handler.*;
import net.shirojr.nemuelch.network.NeMuelchCache;

public class NeMuelchEvents {
    public static void initializeCommon() {
        AttackEvents attackEvents = new AttackEvents();
        SleepEvents sleepEvents = new SleepEvents();
        UseEvents useEvents = new UseEvents();
        ItemEvents itemEvents = new ItemEvents();
        LootEvents lootEvents = new LootEvents();
        ServerPlayerJoinLeaveEvents playerJoinEvents = new ServerPlayerJoinLeaveEvents();
        ServerEntityEvents serverEntityEvents = new ServerEntityEvents();
        AcidEvents acidEvents = new AcidEvents();
        BlockStateEvents blockStateEvents = new BlockStateEvents();
        DayStateEvents dayStateEvents = new DayStateEvents();
        PickedUpItemEvents itemPickUpEvents = new PickedUpItemEvents();

        CommandRegistrationEvents.registerCommon();
        ServerPlayConnectionEvents.JOIN.register(playerJoinEvents);
        ServerPlayConnectionEvents.DISCONNECT.register(playerJoinEvents);
        ServerMiscEvents.initialize();
        AttackEntityCallback.EVENT.register(attackEvents);
        AttackBlockCallback.EVENT.register(attackEvents);
        EntitySleepEvents.START_SLEEPING.register(sleepEvents);
        EntitySleepEvents.STOP_SLEEPING.register(sleepEvents);
        UseEntityCallback.EVENT.register(useEvents);
        UseBlockCallback.EVENT.register(useEvents);
        UseItemCallback.EVENT.register(itemEvents);
        LootTableEvents.MODIFY.register(lootEvents);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(serverEntityEvents);
        AcidCallbacks.IS_DIRECT_CONTACT_PROTECTED.register(acidEvents);
        BlockStateCallbacks.STATE_CHANGED.register(blockStateEvents);
        DayStateCallbacks.ON_DAY_START.register(dayStateEvents);
        DayStateCallbacks.ON_DAY_END.register(dayStateEvents);
        DayStateCallbacks.ON_NIGHT_START.register(dayStateEvents);
        DayStateCallbacks.ON_NIGHT_END.register(dayStateEvents);
        ItemPickupCallbacks.ON_ENTITY_PICKED_UP_ITEM.register(itemPickUpEvents);
    }

    public static void initializeClient() {
        ClientBlockEntityLoadingEvents clientBlockEntityLoadEvents = new ClientBlockEntityLoadingEvents();

        ClientTickEvents.END_CLIENT_TICK.register(new KeyBindEvents());
        ClientPlayConnectionEvents.DISCONNECT.register(new ClientPlayerLeaveEvents());
        ClientTickEvents.END_CLIENT_TICK.register(client -> NeMuelchCache.CAMERA_SHAKE_HANDLER.tick());
        ClientTickEvents.END_CLIENT_TICK.register(new ClientPlayerTickingEvents());
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register(clientBlockEntityLoadEvents);
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(clientBlockEntityLoadEvents);
        RenderEvents.register();
    }
}
