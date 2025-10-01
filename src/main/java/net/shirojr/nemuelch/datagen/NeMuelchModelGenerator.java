package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.custom.CenteredHalfSlabBlock;
import net.shirojr.nemuelch.block.custom.ChimneyBlock;
import net.shirojr.nemuelch.block.custom.HalfSlabBlock;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

import java.util.Optional;

public class NeMuelchModelGenerator extends FabricModelProvider {

    public NeMuelchModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        for (var variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
            Identifier blockId = Registries.BLOCK.getId(variationHolder.getBlock());
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
            Identifier modelContentId = model.upload(variationHolder.getBlock(), textureMap, generator.modelCollector);

            if (variationHolder.getBlock() instanceof HalfSlabBlock) {
                Identifier topStateModelId = new Identifier(
                        variationHolder.getBaseModel().getNamespace(),
                        variationHolder.getBaseModel().getPath() + "_top"
                );
                Model topBlockStateModel = new Model(
                        Optional.of(topStateModelId),
                        Optional.empty(),
                        outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
                );
                topBlockStateModel.upload(variationHolder.getBlock(), "_top", textureMap, generator.modelCollector);
            }


            BlockStateVariantMap blockStateVariantMap;
            if (variationHolder.getBlock() instanceof ChimneyBlock || variationHolder.getBlock() instanceof CenteredHalfSlabBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createAxisRotatedVariantMap();
            } else if (variationHolder.getBlock() instanceof HalfSlabBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates();
            }
            else {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultRotationStates();
            }

            VariantsBlockStateSupplier blockStateSupplier = VariantsBlockStateSupplier
                    .create(variationHolder.getBlock(), BlockStateVariant.create().put(VariantSettings.MODEL, modelContentId))
                    .coordinate(blockStateVariantMap);

            if (variationHolder.getBlock() instanceof HalfSlabBlock) {
                blockStateSupplier = blockStateSupplier.coordinate(BlockStateVariantMap.create(HalfSlabBlock.HALF).register(half -> {
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
