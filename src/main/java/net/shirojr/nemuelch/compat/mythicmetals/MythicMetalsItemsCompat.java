package net.shirojr.nemuelch.compat.mythicmetals;

import net.minecraft.item.Item;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.supportItem.ClimbingPickItem;
import nourl.mythicmetals.item.tools.MythicToolMaterials;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface MythicMetalsItemsCompat {
    List<Item> MYTHIC_METALS_MATERIAL_ITEMS = new ArrayList<>();

    ClimbingPickItem ADAMANTITE_CLIMBING_PICKAXE = registerClimbingPicks(
            "adamantite_climbing_pickaxe",
            new ClimbingPickItem(
                    MythicToolMaterials.ADAMANTITE,
                    3, -2.4F,
                    new Item.Settings().maxCount(1),
                    1800, 6, 40
            )
    );
    ClimbingPickItem COPPER_CLIMBING_PICKAXE = registerClimbingPicks(
            "copper_climbing_pickaxe",
            new ClimbingPickItem(
                    MythicToolMaterials.COPPER,
                    2, -2.9F,
                    new Item.Settings().maxCount(1),
                    60, 2, 10
            )
    );
    ClimbingPickItem MYTHRIL_CLIMBING_PICKAXE = registerClimbingPicks(
            "mythril_climbing_pickaxe",
            new ClimbingPickItem(
                    MythicToolMaterials.MYTHRIL,
                    2, -2.7F,
                    new Item.Settings().maxCount(1),
                    2000, 5.5, 20
            )
    );
    ClimbingPickItem ORICHALCUM_CLIMBING_PICKAXE = registerClimbingPicks(
            "orichalcum_climbing_pickaxe",
            new ClimbingPickItem(
                    MythicToolMaterials.ORICHALCUM,
                    1, -2.8F,
                    new Item.Settings().maxCount(1),
                    4000, 4, 50
            )
    );

    static <T extends ClimbingPickItem> T registerClimbingPicks(String name, T entry) {
        T registeredEntry = NeMuelchItems.registerClimbingPicks(name, entry);
        MYTHIC_METALS_MATERIAL_ITEMS.add(registeredEntry);
        return registeredEntry;
    }

    static void initialize() {
        // static initialisation
        if (!NeMuelch.isMythicMetalsLoaded()) {
            throw new IllegalStateException("Tried to register Tools which need Mythic Metals mod (missing)");
        }
    }
}
