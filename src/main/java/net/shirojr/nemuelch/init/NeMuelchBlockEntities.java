package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.custom.*;

import java.util.List;

public interface NeMuelchBlockEntities {
    BlockEntityType<PestcaneStationBlockEntity> PESTCANE_STATION = register("pestcane_station",
            PestcaneStationBlockEntity::new, NeMuelchBlocks.PESTCANE_STATION);

    BlockEntityType<RopeWinchBlockEntity> ROPER_STATION = register("roper_station",
            RopeWinchBlockEntity::new, NeMuelchBlocks.ROPER);

    BlockEntityType<WandOfSolBlockEntity> WAND_OF_SOL = register("wand_of_sol",
            WandOfSolBlockEntity::new, NeMuelchBlocks.WAND_OF_SOL);

    BlockEntityType<WateringCanBlockEntity> WATERING_CAN = register("watering_can",
            WateringCanBlockEntity::new, NeMuelchBlocks.WATERING_CAN);

    BlockEntityType<DropPotBlockEntity> DROP_BLOCK = register("drop_block",
            DropPotBlockEntity::new, NeMuelchBlocks.DROP_POT);

    BlockEntityType<RottenMeatBlockEntity> ROTTEN_MEAT = register("rotten_meat",
            RottenMeatBlockEntity::new, NeMuelchBlocks.ROTTEN_MEAT);

    BlockEntityType<AdvancedFogBlockEntity> ADVANCED_FOG = register("advanced_fog",
            AdvancedFogBlockEntity::new, NeMuelchBlocks.ADVANCED_FOG);

    BlockEntityType<CrateBlockEntity> CRATE = register("crate",
            CrateBlockEntity::new, NeMuelchBlocks.CRATES);

    BlockEntityType<WaterCrateBlockEntity> WATER_CRATE = register("water_crate",
            WaterCrateBlockEntity::new, NeMuelchBlocks.WATER_CRATE);

    BlockEntityType<CrystalBlockEntity> CRYSTAL = register("crystal",
            CrystalBlockEntity::new, NeMuelchBlocks.CRYSTALS);

    BlockEntityType<CargoCrateBlockEntity> CARGO_CRATE = register("cargo_crate",
            CargoCrateBlockEntity::new, NeMuelchBlocks.CARGO_CRATE);


    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
                                                                       Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name),
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
                                                                       List<? extends Block> blocks) {
        FabricBlockEntityTypeBuilder<T> builder = FabricBlockEntityTypeBuilder.create(factory);
        for (Block block : blocks) {
            builder.addBlock(block);
        }
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name), builder.build());
    }

    static void initialize() {
        // static initialisation
    }
}
