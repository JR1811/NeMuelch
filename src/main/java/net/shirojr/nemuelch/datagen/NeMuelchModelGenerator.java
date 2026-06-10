package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
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

        generator.registerNorthDefaultHorizontalRotation(NeMuelchBlocks.WALL_LANTERN);
        generator.excludeFromSimpleItemModelGeneration(NeMuelchBlocks.WALL_LANTERN);
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
}
