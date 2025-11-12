package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.shirojr.nemuelch.command.*;

public class CommandRegistrationEvents {
    public static void registerCommon() {
        CommandRegistrationCallback.EVENT.register(new RespawnCommands());
        CommandRegistrationCallback.EVENT.register(new MonsterCommands());
        CommandRegistrationCallback.EVENT.register(new ActCommand());
        CommandRegistrationCallback.EVENT.register(new BlightCommands());
        CommandRegistrationCallback.EVENT.register(new MiscItemCommands());
        CommandRegistrationCallback.EVENT.register(new SoundToolCommand());
    }

    public static void registerClient() {
        ClientCommandRegistrationCallback.EVENT.register(FadeClientCommand::register);
    }
}
