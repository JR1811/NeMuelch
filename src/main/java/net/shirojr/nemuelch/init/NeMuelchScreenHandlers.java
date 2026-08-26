package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;
import net.shirojr.nemuelch.screen.handler.PestcaneStationScreenHandler;
import net.shirojr.nemuelch.screen.handler.RopeWinchScreenHandler;

public class NeMuelchScreenHandlers {
    public static ScreenHandlerType<PestcaneStationScreenHandler> PESTCANE_STATION = register(
            "pestcane_station", new ScreenHandlerType<>(PestcaneStationScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA))
    );

    public static ScreenHandlerType<RopeWinchScreenHandler> ROPER = register(
            "roper_station", new ScreenHandlerType<>(RopeWinchScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA))
    );

    public static ScreenHandlerType<CargoCrateScreenHandler> CARGO_CRATE = register(
            "cargo_crate", new ScreenHandlerType<>((syncId, playerInventory) ->
                    new CargoCrateScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY),
                    FeatureSet.of(FeatureFlags.VANILLA)
            )
    );


    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String identifier, ScreenHandlerType<T> screenHandlerType) {
        return Registry.register(Registries.SCREEN_HANDLER, new Identifier(NeMuelch.MOD_ID, identifier), screenHandlerType);
    }

    public static void initialize() {
        // static initialisation
    }
}
