package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.ChimneyBlock;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

import java.util.Optional;

public class NeMuelchModelGenerator extends FabricModelProvider {

    public NeMuelchModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        for (ChimneyBlock chimneyBlock : NeMuelchBlocks.CHIMNEYS) {
            Identifier blockId = Registries.BLOCK.getId(chimneyBlock);
            if (blockId.equals(Registries.BLOCK.getDefaultId())) continue;

            TextureKey outerTextureKey = TextureKey.of("1");
            TextureKey innerTextureKey = TextureKey.of("2");
            TextureKey rimTextureKey = TextureKey.of("3");

            Model model = new Model(
                    Optional.of(NeMuelch.getId("block/base_chimney")),
                    Optional.empty(),
                    outerTextureKey, innerTextureKey, rimTextureKey, TextureKey.PARTICLE
            );

            TextureMap chimneyTextureMap = new TextureMap();

            if (chimneyBlock.getVariant().customParticleTexture() == null) {
                chimneyTextureMap.put(TextureKey.PARTICLE, TextureMap.getId(chimneyBlock.getVariant().parentBlock()));
            } else {
                chimneyTextureMap.put(TextureKey.PARTICLE, chimneyBlock.getVariant().customParticleTexture());
            }
            chimneyTextureMap.put(outerTextureKey, chimneyBlock.getVariant().outerTexture());
            chimneyTextureMap.put(innerTextureKey, chimneyBlock.getVariant().innerTexture());
            chimneyTextureMap.put(rimTextureKey, chimneyBlock.getVariant().rimTexture());

            Identifier modelContentId = model.upload(chimneyBlock, chimneyTextureMap, generator.modelCollector);

            generator.blockStateCollector.accept(
                    VariantsBlockStateSupplier.create(chimneyBlock, BlockStateVariant.create().put(VariantSettings.MODEL, modelContentId))
                            .coordinate(BlockStateModelGenerator.createAxisRotatedVariantMap())

            );
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {

    }
}
