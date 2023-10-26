package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.shirojr.nemuelch.command.SpecialSleepEventCommand;

public class CommandRegistrationEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(SpecialSleepEventCommand::register);
    }
}
