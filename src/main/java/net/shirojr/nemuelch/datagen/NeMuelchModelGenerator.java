package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.custom.*;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

import java.util.Optional;

public class NeMuelchModelGenerator extends FabricModelProvider {

    public NeMuelchModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        for (var variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
            Block block = variationHolder.getBlock();
            Identifier blockId = Registries.BLOCK.getId(block);
            if (blockId.equals(Registries.BLOCK.getDefaultId())) continue;

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
            Identifier modelContentId = model.upload(block, textureMap, generator.modelCollector);

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


            BlockStateVariantMap blockStateVariantMap;
            if (block instanceof ChimneyBlock || block instanceof CenteredVerticalHalfSlabBlock || block instanceof DoublePlatesBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createAxisRotatedVariantMap();
            } else if (block instanceof HalfSlabBlock || block instanceof CenteredHalfSlab || block instanceof VerticalHalfSlabBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates();
            } else {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultRotationStates();
            }

            VariantsBlockStateSupplier blockStateSupplier = VariantsBlockStateSupplier
                    .create(block, BlockStateVariant.create().put(VariantSettings.MODEL, modelContentId))
                    .coordinate(blockStateVariantMap);

            if (block instanceof HalfSlabBlock || block instanceof CenteredHalfSlab) {
                blockStateSupplier = blockStateSupplier.coordinate(BlockStateVariantMap.create(Properties.BLOCK_HALF).register(half -> {
                    if (half.equals(BlockHalf.TOP)) {
                        return BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.of(modelContentId.getNamespace(), modelContentId.getPath() + "_top"));
                    } else {
                        return BlockStateVariant.create().put(VariantSettings.MODEL, modelContentId);
                    }
                }));
            }

            generator.blockStateCollector.accept(blockStateSupplier);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {

    }
}
