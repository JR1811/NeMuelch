package net.shirojr.nemuelch.init;

import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.item.client.*;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class NeMuelchGeckoLibRendering {
    static {
        registerItemRenderers();
        registerArmorRendering();
    }

    private static void registerItemRenderers() {
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.ARKADUS_CANE, new ArkaduscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_CANE, new GladiuscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_BLADE, new GladiusBladeRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.RADIATUM_CANE, new RadiatumcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLOVE_ITEM, new TraininggloveRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.WAND_OF_SOL, new WandOfSolItemRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.WAND_OF_SOL_TANK, new WandOfSolTankItemRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.WAND_OF_SOL_POLE, new WandOfSolPoleItemRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.FORTIFIED_SHIELD, new FortifiedShieldRenderer());
    }

    private static void registerArmorRendering() {
        GeoArmorRenderer.registerArmorRenderer(new PortableBarrelRenderer(), NeMuelchItems.PORTABLE_BARREL);
        GeoArmorRenderer.registerArmorRenderer(new RoyalGuardArmorRenderer(), NeMuelchItems.ROYAL_GUARD_ARMOR_HELMET_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new RoyalGuardArmorRenderer(), NeMuelchItems.ROYAL_GUARD_ARMOR_CHESTPLATE_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new RoyalGuardArmorRenderer(), NeMuelchItems.ROYAL_GUARD_ARMOR_LEGGINGS_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new RoyalGuardArmorRenderer(), NeMuelchItems.ROYAL_GUARD_ARMOR_BOOTS_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new FallenGuardArmorRenderer(), NeMuelchItems.FALLEN_GUARD_ARMOR_HELMET_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new FallenGuardArmorRenderer(), NeMuelchItems.FALLEN_GUARD_ARMOR_CHESTPLATE_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new FallenGuardArmorRenderer(), NeMuelchItems.FALLEN_GUARD_ARMOR_LEGGINGS_ITEM);
        GeoArmorRenderer.registerArmorRenderer(new FallenGuardArmorRenderer(), NeMuelchItems.FALLEN_GUARD_ARMOR_BOOTS_ITEM);
    }

    public static void initialize() {
        // static initialisation
    }
}
