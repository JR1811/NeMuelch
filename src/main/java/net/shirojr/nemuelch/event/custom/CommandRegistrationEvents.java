package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.shirojr.nemuelch.command.SpecialSleepEventCommand;
import net.shirojr.nemuelch.command.HallucinationCommand;

public class CommandRegistrationEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SpecialSleepEventCommand.register(dispatcher, dedicated);
            HallucinationCommand.register(dispatcher, dedicated);
        });
    }
}
