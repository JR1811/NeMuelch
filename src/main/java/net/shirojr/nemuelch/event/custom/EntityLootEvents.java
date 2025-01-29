package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.init.NeMuelchEntities;

public class EntityLootEvents {
    public static void register() {
        LootTableEvents.MODIFY.register(EntityLootEvents::buildDrops);
    }

    private static void buildDrops(ResourceManager resourceManager, LootManager lootManager, Identifier identifier,
                                   LootTable.Builder builder, LootTableSource lootTableSource) {
        if (identifier.equals(Registry.ENTITY_TYPE.getId(NeMuelchEntities.ONION))) {
            LootPool paperPool = LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(0, 2))
                    .conditionally(RandomChanceLootCondition.builder(0.5f))
                    .with(ItemEntry.builder(Items.PAPER)).build();
            LootPool seaPicklePool = LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(0, 1))
                    .conditionally(RandomChanceLootCondition.builder(0.5f))
                    .with(ItemEntry.builder(Items.SEA_PICKLE)).build();

            builder.pool(paperPool);
            builder.pool(seaPicklePool);
        }
    }
}
