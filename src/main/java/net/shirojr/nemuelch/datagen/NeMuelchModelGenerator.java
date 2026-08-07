package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.CrystalBlock;
import net.shirojr.nemuelch.block.custom.SpikeTrapBlock;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;

import java.util.EnumMap;
import java.util.Optional;

public class NeMuelchModelGenerator extends FabricModelProvider {

    public NeMuelchModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerSimpleState(NeMuelchBlocks.WATER_CRATE);
        generator.registerTintableCross(NeMuelchBlocks.ROTTEN_TREE_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
        generator.registerLog(NeMuelchBlocks.ROTTEN_TREE_LOG).log(NeMuelchBlocks.ROTTEN_TREE_LOG);
        generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(NeMuelchBlocks.ROTTEN_MEAT)
                        .coordinate(
                                BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()
                        )
                        .coordinate(
                                BlockStateVariantMap.create(NeMuelchProperties.ROTTEN_MEAT_STAGE).register(stage -> {
                                    String path = "block/rotten_meat";
                                    if (stage > 1)
                                        path += "_" + (stage - 1);   // for performance second stage is the same as default, just with BE
                                    return BlockStateVariant.create().put(VariantSettings.MODEL, NeMuelch.getId(path));
                                })
                        )
        );

        for (CrateBlock crateBlock : NeMuelchBlocks.CRATES) {
            generator.excludeFromSimpleItemModelGeneration(crateBlock);
            VariantsBlockStateSupplier crateBlockStateSupplier = VariantsBlockStateSupplier.create(crateBlock)
                    .coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates())
                    .coordinate(BlockStateVariantMap.create(NeMuelchProperties.CRATE_TYPE)
                            .register(type -> BlockStateVariant.create().put(VariantSettings.MODEL, generateCrateModel(crateBlock, type, generator)))
                    );

            generator.blockStateCollector.accept(crateBlockStateSupplier);
        }

        generator.registerNorthDefaultHorizontalRotation(NeMuelchBlocks.WALL_LANTERN);
        generator.excludeFromSimpleItemModelGeneration(NeMuelchBlocks.WALL_LANTERN);

        for (CrystalBlock crystalBlock : NeMuelchBlocks.CRYSTALS) {
            Identifier id = Registries.BLOCK.getId(crystalBlock);
            generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(crystalBlock)
                    .coordinate(BlockStateVariantMap.create(Properties.WALL_MOUNT_LOCATION, Properties.HORIZONTAL_FACING)
                            .register(WallMountLocation.FLOOR, Direction.NORTH, BlockStateVariant.create())
                            .register(WallMountLocation.FLOOR, Direction.EAST, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90))
                            .register(WallMountLocation.FLOOR, Direction.SOUTH, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180))
                            .register(WallMountLocation.FLOOR, Direction.WEST, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270))
                            .register(WallMountLocation.WALL, Direction.NORTH, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90))
                            .register(WallMountLocation.WALL, Direction.EAST, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                            .register(WallMountLocation.WALL, Direction.SOUTH, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.Y, VariantSettings.Rotation.R180))
                            .register(WallMountLocation.WALL, Direction.WEST, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.Y, VariantSettings.Rotation.R270))
                            .register(WallMountLocation.CEILING, Direction.SOUTH, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180))
                            .register(WallMountLocation.CEILING, Direction.WEST, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                            .register(WallMountLocation.CEILING, Direction.NORTH, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180).put(VariantSettings.Y, VariantSettings.Rotation.R180))
                            .register(WallMountLocation.CEILING, Direction.EAST, BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180).put(VariantSettings.Y, VariantSettings.Rotation.R270))
                    )
                    .coordinate(BlockStateVariantMap.create(NeMuelchProperties.CRYSTAL_STAGE)
                            .register(stage -> BlockStateVariant.create().put(VariantSettings.MODEL, NeMuelch.getId("block/%s_stage_%s".formatted(id.getPath(), stage))))
                    )
            );
            generator.excludeFromSimpleItemModelGeneration(crystalBlock);
        }

        SpikeTrapBlock spikeTrapBlock = NeMuelchBlocks.SPIKE_TRAP;
        generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(spikeTrapBlock, BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockModelId(spikeTrapBlock)))
                        .coordinate(generator.createUpDefaultFacingVariantMap())
                        .coordinate(createEnumModelMap(SpikeTrapBlock.STATE, SpikeTrapBlock.State.getModelIdMapping(spikeTrapBlock)))
        );
        generator.excludeFromSimpleItemModelGeneration(spikeTrapBlock);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(NeMuelchItems.LARD, Models.GENERATED);
        generator.register(NeMuelchItems.SOAP, Models.GENERATED);
        generator.register(NeMuelchItems.CREATIVE_SOAP, Models.GENERATED);
        generator.register(NeMuelchItems.MEAT_LUMP, Models.GENERATED);
        generator.register(NeMuelchItems.COOKED_MEAT_LUMP, Models.GENERATED);
        generator.register(NeMuelchItems.ROTTEN_MEAT_LUMP, Models.GENERATED);
        generator.register(NeMuelchItems.ENTITY_TRANSPORTER, Models.HANDHELD);
        generator.register(NeMuelchItems.REFILLER, Models.HANDHELD);
        generator.register(NeMuelchItems.SOUND_TOOL, Models.HANDHELD);
        generator.register(NeMuelchItems.DISPLACEMENT_TOOL, Models.HANDHELD);
        generator.register(NeMuelchItems.ROPE_MODIFIER, Models.HANDHELD);
        generator.register(NeMuelchItems.COMB, Models.GENERATED);

        for (CrateBlockItem crate : NeMuelchItems.CRATES) {
            if (!(crate.getBlock() instanceof CrateBlock block)) continue;
            generator.register(crate, new Model(
                    Optional.of(NeMuelch.getId("block/" + block.getMaterialPrefix() + "_crate_single")),
                    Optional.empty())
            );
        }

        Identifier builtinEntityId = Identifier.tryParse("minecraft:builtin/entity");
        if (builtinEntityId != null) {
            generator.register(NeMuelchItems.CHAINED_MACE, new Model(Optional.of(builtinEntityId), Optional.empty()));
        }

        for (CrystalBlock crystalBlock : NeMuelchBlocks.CRYSTALS) {
            Identifier parentModelId = Registries.BLOCK.getId(crystalBlock);
            for (int i = 0; i <= NeMuelchProperties.MAX_CRYSTAL_STAGE; i++) {
                Identifier entryId = parentModelId.withSuffixedPath("_stage_" + i);
                Model itemModel = new Model(Optional.of(entryId.withPrefixedPath("block/")), Optional.empty());
                itemModel.upload(entryId.withPrefixedPath("item/"), new TextureMap(), generator.writer);
                // generator.register(crystalBlock.asItem(), new Model(Optional.of(parentModelId), Optional.empty()));
            }
        }

        Identifier exposedSpikeTrapId = NeMuelch.getId("block/spike_trap_exposed");
        generator.register(NeMuelchBlocks.SPIKE_TRAP.asItem(), new Model(Optional.of(exposedSpikeTrapId), Optional.empty()));
    }

    private static Identifier generateCrateModel(CrateBlock crateBlock, CrateBlock.Type type, BlockStateModelGenerator generator) {
        TextureKey baseTextureKey = TextureKey.of("1");
        TextureKey pillerTextureKey = TextureKey.of("2");
        TextureKey pillerTopTextureKey = TextureKey.of("3");

        TextureMap textureMap = new TextureMap();
        textureMap.put(baseTextureKey, Identifier.of("minecraft", "block/" + crateBlock.getMaterialPrefix() + "_planks"));
        textureMap.put(pillerTextureKey, Identifier.of("minecraft", "block/stripped_" + crateBlock.getMaterialPrefix() + "_log"));
        textureMap.put(pillerTopTextureKey, Identifier.of("minecraft", "block/stripped_" + crateBlock.getMaterialPrefix() + "_log_top"));
        textureMap.put(TextureKey.PARTICLE, TextureMap.getId(crateBlock.getBaseMaterial()));

        Model model = new Model(
                Optional.of(type.getParentModel().withPrefixedPath("block/")),
                Optional.empty(),
                baseTextureKey, pillerTextureKey, pillerTopTextureKey, TextureKey.PARTICLE
        );

        return model.upload(
                type.getParentModel().withPrefixedPath("block/" + crateBlock.getMaterialPrefix() + "_"),
                textureMap,
                generator.modelCollector
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Enum<T> & StringIdentifiable> BlockStateVariantMap createEnumModelMap(EnumProperty<T> property, EnumMap<T, Identifier> models) {
        BlockStateVariantMap.SingleProperty<T> propertyHandler = BlockStateVariantMap.create(property);
        models.forEach((key, value) -> propertyHandler.register(key, BlockStateVariant.create().put(VariantSettings.MODEL, value)));
        return propertyHandler;
    }
}
