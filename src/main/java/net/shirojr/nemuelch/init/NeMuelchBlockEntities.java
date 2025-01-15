package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.custom.*;

public class NeMuelchBlockEntities {
    public static BlockEntityType<ParticleEmitterBlockEntity> PARTICLE_EMITTER = register("particle_emitter",
            ParticleEmitterBlockEntity::new, NeMuelchBlocks.PARTICLE_EMITTER);

    public static BlockEntityType<SoundEmitterBlockEntity> SOUND_EMITTER = register("sound_emitter",
            SoundEmitterBlockEntity::new, NeMuelchBlocks.SOUND_EMITTER);

    public static BlockEntityType<PestcaneStationBlockEntity> PESTCANE_STATION = register("pestcane_station",
            PestcaneStationBlockEntity::new, NeMuelchBlocks.PESTCANE_STATION);

    public static BlockEntityType<RopeWinchBlockEntity> ROPER_STATION = register("roper_station",
            RopeWinchBlockEntity::new, NeMuelchBlocks.ROPER);

    public static BlockEntityType<WandOfSolBlockEntity> WAND_OF_SOL = register("wand_of_sol",
            WandOfSolBlockEntity::new, NeMuelchBlocks.WAND_OF_SOL);

    public static BlockEntityType<WateringCanBlockEntity> WATERING_CAN = register("watering_can",
            WateringCanBlockEntity::new, NeMuelchBlocks.WATERING_CAN);

    public static BlockEntityType<DropPotBlockEntity> DROP_BLOCK = register("drop_block",
            DropPotBlockEntity::new, NeMuelchBlocks.DROP_POT);


    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
                                                                       Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name),
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    public static void initialize() {
        // static initialisation
    }
}
