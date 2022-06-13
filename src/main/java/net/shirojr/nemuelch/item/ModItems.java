package net.shirojr.nemuelch.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public class ModItems {

    //new items

    public static final Item GREEN_MUELCH = registerItem("green_muelch",
            new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(ModFoodComponents.GREEN_MILK)));

    public static final Item BROWN_MUELCH = registerItem("brown_muelch",
            new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(ModFoodComponents.BROWN_MILK)));

    public static final Item BLUE_MUELCH = registerItem("blue_muelch",
            new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(ModFoodComponents.BLUE_MILK)));

    public static final Item PINK_MUELCH = registerItem("pink_muelch",
            new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(ModFoodComponents.PINK_MILK)));

    public static final Item YELLOW_MUELCH = registerItem("yellow_muelch",
            new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(ModFoodComponents.YELLOW_MILK)));



    //preparing items for loading

    private static Item registerItem(String name, Item item) {

        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name), item);

    }

    public static void registerModItems() {
        NeMuelch.LOGGER.info("Registering Milk Items for " + NeMuelch.MOD_ID + " Mod");
    }
}
