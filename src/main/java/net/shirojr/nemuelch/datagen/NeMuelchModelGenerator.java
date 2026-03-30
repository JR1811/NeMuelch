package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.*;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.util.VariationHolder;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;

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


        for (var variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
            Block block = variationHolder.getBlock();
            Identifier blockId = Registries.BLOCK.getId(block);
            if (blockId.equals(Registries.BLOCK.getDefaultId())) continue;

            if (block instanceof SmallFenceBlock) {
                generateSmallFence(variationHolder, block, generator);
                continue;
            }

            Identifier modelId = generateModel(variationHolder, block, generator);


            BlockStateVariantMap blockStateVariantMap;
            if (block instanceof ChimneyBlock || block instanceof CenteredVerticalHalfSlabBlock || block instanceof DoublePlatesBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createAxisRotatedVariantMap();
            } else if (block instanceof HalfSlabBlock || block instanceof CenteredHalfSlab || block instanceof VerticalHalfSlabBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates();
            } else {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultRotationStates();
            }

            VariantsBlockStateSupplier blockStateSupplier = VariantsBlockStateSupplier
                    .create(block, BlockStateVariant.create().put(VariantSettings.MODEL, modelId))
                    .coordinate(blockStateVariantMap);

            if (block instanceof HalfSlabBlock || block instanceof CenteredHalfSlab) {
                blockStateSupplier = blockStateSupplier.coordinate(BlockStateVariantMap.create(Properties.BLOCK_HALF).register(half -> {
                    if (half.equals(BlockHalf.TOP)) {
                        return BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.of(modelId.getNamespace(), modelId.getPath() + "_top"));
                    } else {
                        return BlockStateVariant.create().put(VariantSettings.MODEL, modelId);
                    }
                }));
            }
            generator.blockStateCollector.accept(blockStateSupplier);
        }
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

        for (CrateBlockItem crate : NeMuelchItems.CRATES) {
            if (!(crate.getBlock() instanceof CrateBlock block)) continue;

            generator.register(crate, new Model(
                    Optional.of(NeMuelch.getId("block/" + block.getMaterialPrefix() + "_crate_single")),
                    Optional.empty())
            );
        }
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

    private static Identifier generateModel(VariationHolder variationHolder, Block block, BlockStateModelGenerator generator) {
        TextureKey outerTextureKey = TextureKey.of("1");
        TextureKey innerTextureKey = TextureKey.of("2");
        TextureKey rimTextureKey = TextureKey.of("3");

        TextureMap textureMap = new TextureMap();

        if (variationHolder.getVariant().customParticleTexture() == null) {
            textureMap.put(TextureKey.PARTICLE, TextureMap.getId(variationHolder.getVariant().parentBlock()));
        } else {
            textureMap.put(TextureKey.PARTICLE, variationHolder.getVariant().customParticleTexture());
        }
        textureMap.put(outerTextureKey, variationHolder.getVariant().outerTexture());
        textureMap.put(innerTextureKey, variationHolder.getVariant().innerTexture());
        textureMap.put(rimTextureKey, variationHolder.getVariant().rimTexture());

        Model model = new Model(
                Optional.of(variationHolder.getBaseModel()),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );

        if (block instanceof HalfSlabBlock || block instanceof CenteredHalfSlab) {
            Identifier topStateModelId = new Identifier(
                    variationHolder.getBaseModel().getNamespace(),
                    variationHolder.getBaseModel().getPath() + "_top"
            );
            Model topBlockStateModel = new Model(
                    Optional.of(topStateModelId),
                    Optional.empty(),
                    outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
            );
            topBlockStateModel.upload(block, "_top", textureMap, generator.modelCollector);
        }

        return model.upload(block, textureMap, generator.modelCollector);
    }

    private void generateSmallFence(VariationHolder variationHolder, Block block, BlockStateModelGenerator generator) {
        TextureKey outerTextureKey = TextureKey.of("1");
        TextureKey innerTextureKey = TextureKey.of("2");
        TextureKey rimTextureKey = TextureKey.of("3");
        TextureMap textureMap = new TextureMap();
        if (variationHolder.getVariant().customParticleTexture() == null) {
            textureMap.put(TextureKey.PARTICLE, TextureMap.getId(variationHolder.getVariant().parentBlock()));
        } else {
            textureMap.put(TextureKey.PARTICLE, variationHolder.getVariant().customParticleTexture());
        }
        textureMap.put(outerTextureKey, variationHolder.getVariant().outerTexture());
        textureMap.put(innerTextureKey, variationHolder.getVariant().innerTexture());
        textureMap.put(rimTextureKey, variationHolder.getVariant().rimTexture());


        Model modelInventory = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_inventory")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );
        Model modelPost = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_post")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );
        Model modelSideNorth = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_side_north")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );
        Model modelSideEast = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_side_east")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );
        Model modelSideSouth = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_side_south")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );
        Model modelSideWest = new Model(
                Optional.of(variationHolder.getBaseModel().withSuffixedPath("_side_west")),
                Optional.empty(),
                outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
        );

        Identifier modelInventoryId = modelInventory.upload(block, "_inventory", textureMap, generator.modelCollector);
        Identifier modelPostId = modelPost.upload(block, "_post", textureMap, generator.modelCollector);
        Identifier modelSideNorthId = modelSideNorth.upload(block, "side_north", textureMap, generator.modelCollector);
        Identifier modelSideEastId = modelSideEast.upload(block, "side_east", textureMap, generator.modelCollector);
        Identifier modelSideSouthId = modelSideSouth.upload(block, "side_south", textureMap, generator.modelCollector);
        Identifier modelSideWestId = modelSideWest.upload(block, "side_west", textureMap, generator.modelCollector);

        generator.blockStateCollector.accept(
                MultipartBlockStateSupplier.create(block)
                        .with(BlockStateVariant.create().put(VariantSettings.MODEL, modelPostId))
                        .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, modelSideNorthId).put(VariantSettings.UVLOCK, false))
                        .with(When.create().set(Properties.EAST, true), BlockStateVariant.create().put(VariantSettings.MODEL, modelSideEastId).put(VariantSettings.UVLOCK, false))
                        .with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, modelSideSouthId).put(VariantSettings.UVLOCK, false))
                        .with(When.create().set(Properties.WEST, true), BlockStateVariant.create().put(VariantSettings.MODEL, modelSideWestId).put(VariantSettings.UVLOCK, false))
        );

        generator.registerParentedItemModel(block, modelInventoryId);
    }
}
