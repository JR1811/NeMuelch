package net.shirojr.nemuelch.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;

public class NeMuelchBlockEntities {

    public static BlockEntityType<PestcaneStationBlockEntity> PESTCANE_STATION;

    public static void registerBlockEntities() {

        PESTCANE_STATION = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "pestcane_station"),
                FabricBlockEntityTypeBuilder.create(PestcaneStationBlockEntity::new,
                        NeMuelchBlocks.PESTCANE_STATION).build(null));
    }
}
