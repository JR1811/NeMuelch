package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.shirojr.nemuelch.command.*;

public class CommandRegistrationEvents {
    public static void registerCommon() {
        CommandRegistrationCallback.EVENT.register(new RespawnCommands());
        CommandRegistrationCallback.EVENT.register(new MonsterCommands());
        CommandRegistrationCallback.EVENT.register(new ActCommand());
        CommandRegistrationCallback.EVENT.register(new BlightCommands());
        CommandRegistrationCallback.EVENT.register(new MiscItemCommands());
        CommandRegistrationCallback.EVENT.register(new MiscEntityCommands());
        CommandRegistrationCallback.EVENT.register(new MiscSoundCommands());
        CommandRegistrationCallback.EVENT.register(new SoundToolCommand());
        CommandRegistrationCallback.EVENT.register(new ShaderServerCommand());
        CommandRegistrationCallback.EVENT.register(new CameraShakeServerCommand());
        CommandRegistrationCallback.EVENT.register(new OccasionCommands());
        CommandRegistrationCallback.EVENT.register(new RopeCommands());
        CommandRegistrationCallback.EVENT.register(new FleetingNotesCommand());
        CommandRegistrationCallback.EVENT.register(new BlockFinderCommands());
        CommandRegistrationCallback.EVENT.register(new AcidCommands());
        CommandRegistrationCallback.EVENT.register(new ServerUtilCommands());
        CommandRegistrationCallback.EVENT.register(new NeMuelchItemModificationCommands());
        CommandRegistrationCallback.EVENT.register(new LoginCommands());
        CommandRegistrationCallback.EVENT.register(new FadeZoneCommands());
        CommandRegistrationCallback.EVENT.register(new BlockDirectMessageCommands());
        CommandRegistrationCallback.EVENT.register(new NotificationZoneCommands());
        CommandRegistrationCallback.EVENT.register(new ClimbingPreventionZoneCommands());
        CommandRegistrationCallback.EVENT.register(new ExplosionRefillerCommands());
    }

    public static void registerClient() {
        // ClientCommandRegistrationCallback.EVENT.register(FadeClientCommand::register);
    }
}
