package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.custom.ChimneyBlock;
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

            Model model = new Model(
                    Optional.of(variationHolder.getBaseModel()),
                    Optional.empty(),
                    outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
            );

            TextureMap chimneyTextureMap = new TextureMap();

            if (variationHolder.getVariant().customParticleTexture() == null) {
                chimneyTextureMap.put(TextureKey.PARTICLE, TextureMap.getId(variationHolder.getVariant().parentBlock()));
            } else {
                chimneyTextureMap.put(TextureKey.PARTICLE, variationHolder.getVariant().customParticleTexture());
            }
            chimneyTextureMap.put(outerTextureKey, variationHolder.getVariant().outerTexture());
            chimneyTextureMap.put(innerTextureKey, variationHolder.getVariant().innerTexture());
            chimneyTextureMap.put(rimTextureKey, variationHolder.getVariant().rimTexture());

            Identifier modelContentId = model.upload(variationHolder.getBlock(), chimneyTextureMap, generator.modelCollector);

            BlockStateVariantMap blockStateVariantMap;
            if (variationHolder.getBlock() instanceof ChimneyBlock) {
                blockStateVariantMap = BlockStateModelGenerator.createAxisRotatedVariantMap();
            } else {
                blockStateVariantMap = BlockStateModelGenerator.createNorthDefaultRotationStates();
            }
            generator.blockStateCollector.accept(
                    VariantsBlockStateSupplier.create(variationHolder.getBlock(), BlockStateVariant.create().put(VariantSettings.MODEL, modelContentId))
                            .coordinate(blockStateVariantMap)

            );
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {

    }
}
