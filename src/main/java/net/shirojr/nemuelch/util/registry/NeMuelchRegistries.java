package net.shirojr.nemuelch.util.registry;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.shirojr.nemuelch.command.BodyPartCommand;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import net.shirojr.nemuelch.event.NeMuelchPlayerEventCopyFrom;

public class NeMuelchRegistries {

    public static void register() {
        registerAttributes();
        registerCommands();
        registerEvents();
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(NeMuelchEntities.ONION, OnionEntity.setAttributes());
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(BodyPartCommand::register);
    }

    private static void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register(new NeMuelchPlayerEventCopyFrom());
    }
}
