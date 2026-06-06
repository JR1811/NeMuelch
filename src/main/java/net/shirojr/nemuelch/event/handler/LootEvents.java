package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.init.NeMuelchItems;

public class LootEvents implements LootTableEvents.Modify {
    @Override
    public void modifyLootTable(ResourceManager resourceManager, LootManager lootManager, Identifier identifier,
                                LootTable.Builder builder, LootTableSource source) {
        if (!source.isBuiltin() || !identifier.equals(EntityType.PIG.getLootTableId())) return;
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceWithLootingLootCondition.builder(0.1f, 0.2f))
                .with(ItemEntry.builder(NeMuelchItems.LARD));
        builder.pool(poolBuilder);
    }
}
