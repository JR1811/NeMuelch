package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.shirojr.nemuelch.command.FadeClientCommand;
import net.shirojr.nemuelch.command.RespawnCommands;
import net.shirojr.nemuelch.command.SpecialSleepEventCommand;

public class CommandRegistrationEvents {
    public static void registerCommon() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SpecialSleepEventCommand.register(dispatcher, registryAccess, environment);
            RespawnCommands.register(dispatcher, registryAccess, environment);
        });
    }

    public static void registerClient() {
        ClientCommandRegistrationCallback.EVENT.register(FadeClientCommand::register);
    }
}
