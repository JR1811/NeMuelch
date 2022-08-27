package net.shirojr.nemuelch.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.PestcaneItem;
import net.shirojr.nemuelch.item.custom.helperItem.EntityTransportToolItem;
import net.shirojr.nemuelch.item.custom.helperItem.EntityTransportToolItem;
import net.shirojr.nemuelch.item.custom.helperItem.RefillToolItem;
import net.shirojr.nemuelch.item.custom.muelchItem.*;

public class NeMuelchItems {

    //new items

    public static final Item GREEN_MUELCH = registerItem("green_muelch",
            new NeMuelchGreenItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.GREEN_MILK).maxCount(1)));

    public static final Item BROWN_MUELCH = registerItem("brown_muelch",
            new NeMuelchBrownItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.BROWN_MILK).maxCount(1)));

    public static final Item BLUE_MUELCH = registerItem("blue_muelch",
            new NeMuelchBlueItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.BLUE_MILK).maxCount(1)));

    public static final Item PINK_MUELCH = registerItem("pink_muelch",
            new NeMuelchPinkItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.PINK_MILK).maxCount(1)));

    public static final Item YELLOW_MUELCH = registerItem("yellow_muelch",
            new NeMuelchYellowItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.YELLOW_MILK).maxCount(1)));

    public static final Item PURPLE_MUELCH = registerItem("purple_muelch",
            new NeMuelchPurpleItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.PURPLE_MILK).maxCount(1)));


    public static final Item PEST_CANE = registerItem("pestcane",
            new PestcaneItem(new FabricItemSettings().group(NeMuelchItemGroup.CANES).maxCount(1)));


    public static final Item REFILLER_TOOL = registerItem("refiller_tool",
            new RefillToolItem(new FabricItemSettings().group(NeMuelchItemGroup.HELPERTOOLS).maxCount(1)));

    public static final Item ENTITYTRANSPORT_TOOL = registerItem("entity_transport_tool",
            new EntityTransportToolItem(new FabricItemSettings().group(NeMuelchItemGroup.HELPERTOOLS).maxCount(1)));


    //preparing items for loading

    private static Item registerItem(String name, Item item) {

        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name), item);

    }

    public static void registerModItems() {
        NeMuelch.LOGGER.info("Registering " + NeMuelch.MOD_ID + " Mod");
    }
}
