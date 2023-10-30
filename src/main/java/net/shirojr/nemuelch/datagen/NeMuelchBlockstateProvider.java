package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.util.NeMuelchProperties;

public class NeMuelchBlockstateProvider extends FabricModelProvider {

    public NeMuelchBlockstateProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.blockStateCollector
                .accept(VariantsBlockStateSupplier
                        .create(NeMuelchBlocks.QUARTER_SPLIT_TNT)
                        .coordinate(horizontalFacingMapForRotation())
                        .coordinate(intMapForModel()));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(NeMuelchItems.TNT_STICK, Models.GENERATED);
    }

    private BlockStateVariantMap horizontalFacingMapForRotation() {
        return BlockStateVariantMap.create(Properties.HORIZONTAL_FACING).register(direction -> switch (direction) {
            case EAST -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90);
            case SOUTH -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180);
            case WEST -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270);
            default -> BlockStateVariant.create();
        });
    }

    private BlockStateVariantMap intMapForModel() {

        return BlockStateVariantMap.create(NeMuelchProperties.QUARTER_SPLIT_PARTS).register(parts -> {
            String file = String.format("block/split_tnt_%s", parts);
            return BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(NeMuelch.MOD_ID, file));
        });

    }

}
