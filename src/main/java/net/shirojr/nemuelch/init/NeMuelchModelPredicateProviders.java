package net.shirojr.nemuelch.init;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.item.custom.supportItem.WateringCanItem;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

@SuppressWarnings("SameParameterValue")
public class NeMuelchModelPredicateProviders {
    static {
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_COPPER, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_IRON, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_GOLD, new Identifier("filled"));
        registerWateringCanProvider(NeMuelchItems.WATERING_CAN_DIAMOND, new Identifier("filled"));
        registerFortifiedShield(NeMuelchItems.FORTIFIED_SHIELD, new Identifier("blocking"));
    }

    private static void registerWateringCanProvider(Item item, Identifier identifier) {
        if (item == null) return;
        ModelPredicateProviderRegistry.register(item, identifier, (itemStack, clientWorld, livingEntity, seed) -> {
            if (!(item instanceof WateringCanItem) || livingEntity == null) return 0;
            WateringCanHelper.ItemMaterial itemMaterial = WateringCanHelper.getItemMaterial(itemStack);
            if (itemMaterial == null) return 0;
            if (WateringCanHelper.readNbtFillState(itemStack) >= itemMaterial.getCapacity()) {
                return 1.0f;
            }
            return 0.0f;
        });
    }

    private static void registerFortifiedShield(Item item, Identifier identifier) {
        if (item == null) return;
        ModelPredicateProviderRegistry.register(item, identifier, (itemStack, clientWorld, livingEntity, seed) ->
                livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F);
    }

    public static void initialize() {
        // static initialisation
    }
}
