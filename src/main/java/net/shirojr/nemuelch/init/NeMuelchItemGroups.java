package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CrystalBlockItem;

import java.util.ArrayList;
import java.util.List;

public class NeMuelchItemGroups {
    public static final RegistryKey<ItemGroup> NEMUELCH = register("nemuelch",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(NeMuelchItems.GREEN_MUELCH))
                    .displayName(Text.translatable("itemGroup.nemuelch.nemuelch"))
                    .build()
    );

    static {
        addItemsToGroup(NeMuelchItems.ALL, NEMUELCH);
        addItemsToGroup(NeMuelchItems.COMBAT, ItemGroups.COMBAT);
        addItemsToGroup(NeMuelchItems.TOOLS, ItemGroups.TOOLS);
        addItemsToGroup(NeMuelchItems.FOOD_AND_DRINK, ItemGroups.FOOD_AND_DRINK);

        addItemStacksToGroup(
                NeMuelchPotions.ALL_POTIONS.stream()
                        .map(potion -> PotionUtil.setPotion(Items.POTION.getDefaultStack(), potion))
                        .toList(),
                ItemGroups.FOOD_AND_DRINK, NEMUELCH
        );

        List<ItemStack> crystalStacks = new ArrayList<>();
        for (int i = 0; i <= NeMuelchProperties.MAX_CRYSTAL_STAGE; i++) {
            for (CrystalBlockItem crystalItem : NeMuelchItems.CRYSTALS) {
                ItemStack crystalStack = crystalItem.getDefaultStack();
                CrystalBlockItem.setStage(crystalStack, i);
                crystalStacks.add(crystalStack);
            }
        }
        addItemStacksToGroup(crystalStacks, NEMUELCH);
    }

    @SafeVarargs
    private static void addItemsToGroup(List<Item> toBeAdded, RegistryKey<ItemGroup>... groups) {
        for (RegistryKey<ItemGroup> group : groups) {
            ItemGroupEvents.modifyEntriesEvent(group).register(entries -> {
                for (Item entry : toBeAdded) {
                    entries.add(entry);
                }
            });
        }
    }

    @SafeVarargs
    private static void addItemStacksToGroup(List<ItemStack> toBeAdded, RegistryKey<ItemGroup>... groups) {
        for (RegistryKey<ItemGroup> group : groups) {
            ItemGroupEvents.modifyEntriesEvent(group).register(entries -> {
                for (ItemStack entry : toBeAdded) {
                    entries.add(entry);
                }
            });
        }
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
