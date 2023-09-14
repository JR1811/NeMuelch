package net.shirojr.nemuelch.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;

public class NeMuelchBlockEntities {

    public static BlockEntityType<ParticleEmitterBlockEntity> PARTICLE_EMITTER;
    public static BlockEntityType<SoundEmitterBlockEntity> SOUND_EMITTER;
    public static BlockEntityType<PestcaneStationBlockEntity> PESTCANE_STATION;
    public static BlockEntityType<RopeWinchBlockEntity> ROPER_STATION;
    public static BlockEntityType<WandOfSolBlockEntity> WAND_OF_SOL;
    public static BlockEntityType<WateringCanBlockEntity> WATERING_CAN;

    public static void registerBlockEntities() {

        PESTCANE_STATION = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "pestcane_station"),
                FabricBlockEntityTypeBuilder.create(PestcaneStationBlockEntity::new,
                        NeMuelchBlocks.PESTCANE_STATION).build(null));

        ROPER_STATION = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "roper_station"),
                FabricBlockEntityTypeBuilder.create(RopeWinchBlockEntity::new,
                        NeMuelchBlocks.ROPER).build(null));

        PARTICLE_EMITTER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "particle_emitter"),
                FabricBlockEntityTypeBuilder.create(ParticleEmitterBlockEntity::new,
                        NeMuelchBlocks.PARTICLE_EMITTER).build(null));

        SOUND_EMITTER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "sound_emitter"),
                FabricBlockEntityTypeBuilder.create(SoundEmitterBlockEntity::new,
                        NeMuelchBlocks.SOUND_EMITTER).build(null));

        WAND_OF_SOL = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "wand_of_sol"),
                FabricBlockEntityTypeBuilder.create(WandOfSolBlockEntity::new,
                        NeMuelchBlocks.WAND_OF_SOL).build(null));

        WATERING_CAN = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(NeMuelch.MOD_ID, "watering_can"),
                FabricBlockEntityTypeBuilder.create(WateringCanBlockEntity::new,
                        NeMuelchBlocks.WATERING_CAN).build(null));

    }
}
