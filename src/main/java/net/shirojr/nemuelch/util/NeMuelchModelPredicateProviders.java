package net.shirojr.nemuelch.util;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.supportItem.WateringCanItem;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

public class NeMuelchModelPredicateProviders {
    public static void register() {
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_COPPER, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_IRON, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_GOLD, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_DIAMOND, new Identifier("filled"));
    }

    private static void registerWateringCanProvider(Item item, Identifier identifier) {
        if (item == null) return;
        ModelPredicateProviderRegistry.register(item, identifier, (itemStack, clientWorld, livingEntity, seed) -> {
            if (!(item instanceof WateringCanItem) || livingEntity == null) return 0;
            if (WateringCanHelper.readNbtFillState(itemStack) >= WateringCanHelper.getItemMaterial(itemStack).getCapacity()) {
                return 1.0f;
            }
            return 0.0f;
        });
    }
}
