package net.shirojr.nemuelch.init;

import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchPaintings {
    public static final PaintingMotive MAP_AXE_ISLAND = registerPainting("painting_axe_island", new PaintingMotive(64, 64));
    public static final PaintingMotive MAP_BEAST_BAY = registerPainting("painting_beast_bay", new PaintingMotive(64, 64));
    public static final PaintingMotive MAP_GRUENTAL = registerPainting("painting_gruental", new PaintingMotive(64, 64));

    private static PaintingMotive registerPainting(String name, PaintingMotive paintingMotive) {
        return Registry.register(Registry.PAINTING_MOTIVE, new Identifier(NeMuelch.MOD_ID, name), paintingMotive);
    }

    public static void initialize() {
        // static initialisation
    }
}
