package net.shirojr.nemuelch.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchScreenHandlers {
    public static ScreenHandlerType<PestcaneStationScreenHandler> PESTCANE_STATION_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        PESTCANE_STATION_SCREEN_HANDLER =
                ScreenHandlerRegistry.registerSimple(new Identifier(NeMuelch.MOD_ID, "mythril_blaster"),
                        PestcaneStationScreenHandler::new);
    }
}
