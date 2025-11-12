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


    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
                                                                       Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name),
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    static void initialize() {
        // static initialisation
    }
}
