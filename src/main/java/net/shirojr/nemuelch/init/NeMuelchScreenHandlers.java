package net.shirojr.nemuelch.init;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.ParticleEmitterBlockScreenHandler;
import net.shirojr.nemuelch.screen.handler.PestcaneStationScreenHandler;
import net.shirojr.nemuelch.screen.handler.RopeWinchScreenHandler;

public class NeMuelchScreenHandlers {
    public static ScreenHandlerType<PestcaneStationScreenHandler> PESTCANE_STATION_SCREEN_HANDLER = register(
            "pestcane_station", new ScreenHandlerType<>(PestcaneStationScreenHandler::new)
    );

    public static ScreenHandlerType<RopeWinchScreenHandler> ROPER_SCREEN_HANDLER = register(
            "roper_station", new ScreenHandlerType<>(RopeWinchScreenHandler::new));

    public static ScreenHandlerType<ParticleEmitterBlockScreenHandler> PARTICLE_EMITTER_SCREEN_HANDLER = register(
            "particle_emitter_block", new ScreenHandlerType<>((syncId, playerInventory) -> new ParticleEmitterBlockScreenHandler(syncId, playerInventory.player))
    );


    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String identifier, ScreenHandlerType<T> screenHandlerType) {
        return Registry.register(Registry.SCREEN_HANDLER, new Identifier(NeMuelch.MOD_ID, identifier), screenHandlerType);
    }

    public static void initialize() {
        // static initialisation
    }
}
