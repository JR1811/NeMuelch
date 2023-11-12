package net.shirojr.nemuelch.screen.handler;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchScreenHandlers {
    public static ScreenHandlerType<PestcaneStationScreenHandler> PESTCANE_STATION_SCREEN_HANDLER;
    public static ScreenHandlerType<RopeWinchScreenHandler> ROPER_SCREEN_HANDLER;
    public static ScreenHandlerType<ParticleEmitterBlockScreenHandler> ÜARTICLE_EMITTER_SCREEN_HANDLER;


    public static void registerAllScreenHandlers() {
        PESTCANE_STATION_SCREEN_HANDLER =
                ScreenHandlerRegistry.registerSimple(new Identifier(NeMuelch.MOD_ID, "pestcane_station"),
                        PestcaneStationScreenHandler::new);

        ROPER_SCREEN_HANDLER =
                ScreenHandlerRegistry.registerSimple(new Identifier(NeMuelch.MOD_ID, "roper_station"),
                        RopeWinchScreenHandler::new);
    }
}
