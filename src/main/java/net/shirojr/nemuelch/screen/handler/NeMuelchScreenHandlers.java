package net.shirojr.nemuelch.screen.handler;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchScreenHandlers {
    public static ScreenHandlerType<PestcaneStationScreenHandler> PESTCANE_STATION_SCREEN_HANDLER;
    public static ScreenHandlerType<RopeWinchScreenHandler> ROPER_SCREEN_HANDLER;
    public static ScreenHandlerType<ParticleEmitterBlockScreenHandler> PARTICLE_EMITTER_SCREEN_HANDLER = registerHandlers(
            "particle_emitter_block", new ScreenHandlerType<>((syncId, playerInventory) -> new ParticleEmitterBlockScreenHandler(syncId, playerInventory.player))
    );


    public static void registerAllScreenHandlers() {
        PESTCANE_STATION_SCREEN_HANDLER =
                ScreenHandlerRegistry.registerSimple(new Identifier(NeMuelch.MOD_ID, "pestcane_station"),
                        PestcaneStationScreenHandler::new);

        ROPER_SCREEN_HANDLER =
                ScreenHandlerRegistry.registerSimple(new Identifier(NeMuelch.MOD_ID, "roper_station"),
                        RopeWinchScreenHandler::new);

    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> registerHandlers(String identifier, ScreenHandlerType<T> screenHandlerType) {
        return Registry.register(Registry.SCREEN_HANDLER, new Identifier(NeMuelch.MOD_ID, identifier), screenHandlerType);
    }
}
