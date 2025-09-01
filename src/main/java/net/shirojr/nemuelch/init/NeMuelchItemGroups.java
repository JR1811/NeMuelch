package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.NeMuelch;

import java.util.List;

public class NeMuelchItemGroups {
    public static final RegistryKey<ItemGroup> NEMUELCH = register("nemuelch",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(NeMuelchItems.GREEN_MUELCH))
                    .displayName(Text.translatable("itemGroup.nemuelch.nemuelch"))
                    .build()
    );
    public static final RegistryKey<ItemGroup> NEMUELCH_VARIATION_BLOCKS = register("nemuelch_variations",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(NeMuelchItems.PINK_MUELCH))
                    .displayName(Text.translatable("itemGroup.nemuelch.nemuelch_block_variations"))
                    .build()
    );

    static {
        addItemsToGroup(NEMUELCH, NeMuelchItems.NEMUELCH_ITEMS);
        addItemsToGroup(NEMUELCH_VARIATION_BLOCKS, NeMuelchItems.NEMUELCH_VARIATION_BLOCK_ITEMS);
        addItemsToGroup(ItemGroups.COMBAT, NeMuelchItems.COMBAT);
        addItemsToGroup(ItemGroups.TOOLS, NeMuelchItems.TOOLS);
        addItemsToGroup(ItemGroups.FOOD_AND_DRINK, NeMuelchItems.FOOD_AND_DRINK);
    }

    private static void addItemsToGroup(RegistryKey<ItemGroup> group, List<Item> toBeAdded) {
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> {
            for (Item entry : toBeAdded) {
                entries.add(entry);
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryKey<ItemGroup> register(String name, ItemGroup group) {
        Registry.register(Registries.ITEM_GROUP, NeMuelch.getId(name), group);
        return RegistryKey.of(Registries.ITEM_GROUP.getKey(), NeMuelch.getId(name));
    }

    public static void initialize() {
        // static initialisation
    }
}
