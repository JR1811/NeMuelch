package net.shirojr.nemuelch.init;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

@SuppressWarnings("unused")
public class NeMuelchPaintings {
    public static final PaintingVariant MAP_AXE_ISLAND = registerPainting("painting_axe_island", new PaintingVariant(64, 64));
    public static final PaintingVariant MAP_BEAST_BAY = registerPainting("painting_beast_bay", new PaintingVariant(64, 64));
    public static final PaintingVariant MAP_GRUENTAL = registerPainting("painting_gruental", new PaintingVariant(64, 64));

    private static PaintingVariant registerPainting(String name, PaintingVariant paintingMotive) {
        return Registry.register(Registries.PAINTING_VARIANT, new Identifier(NeMuelch.MOD_ID, name), paintingMotive);
    }

    public static void initialize() {
        // static initialisation
    }
}
