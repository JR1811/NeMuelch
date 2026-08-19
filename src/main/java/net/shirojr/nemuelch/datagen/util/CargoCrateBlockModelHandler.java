package net.shirojr.nemuelch.datagen.util;

import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.station.CargoCrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Optional;

public class CargoCrateBlockModelHandler {
    private static final String FALLBACK_TEXTURE = "cargo_crate_side_mid_mid";
    private static final Model CARGO_CRATE_PART_MODEL = new Model(
            Optional.of(new Identifier("block/cube")),
            Optional.empty(),
            TextureKey.PARTICLE, TextureKey.DOWN, TextureKey.UP,
            TextureKey.NORTH, TextureKey.SOUTH, TextureKey.WEST, TextureKey.EAST
    );

    public static void generateBlockStateModels(BlockStateModelGenerator generator) {
        Block block = NeMuelchBlocks.CARGO_CRATE;
        MultipartBlockStateSupplier stateSupplier = MultipartBlockStateSupplier.create(block);
        HashMap<String, Identifier> modelCache = new HashMap<>();

        for (Direction facing : Direction.values()) {
            Direction.Axis frontAxis = facing.getAxis();
            for (int offsetX = 0; offsetX <= 2; offsetX++) {
                for (int offsetY = 0; offsetY <= 2; offsetY++) {
                    for (int offsetZ = 0; offsetZ <= 2; offsetZ++) {
                        String cacheKey = frontAxis.getName() + "_" + offsetX + offsetY + offsetZ;
                        Identifier modelId = modelCache.get(cacheKey);
                        if (modelId == null) {
                            modelId = uploadPartModel(generator, block, frontAxis, offsetX, offsetY, offsetZ);
                            modelCache.put(cacheKey, modelId);
                        }

                        stateSupplier.with(
                                When.create()
                                        .set(CargoCrateBlock.FACING, facing)
                                        .set(CargoCrateBlock.OFFSET_X, offsetX)
                                        .set(CargoCrateBlock.OFFSET_Y, offsetY)
                                        .set(CargoCrateBlock.OFFSET_Z, offsetZ),
                                BlockStateVariant.create().put(VariantSettings.MODEL, modelId)
                        );
                    }
                }
            }
        }
        generator.blockStateCollector.accept(stateSupplier);
    }

    private static Identifier uploadPartModel(BlockStateModelGenerator generator, Block block,
                                              Direction.Axis frontAxis, int ox, int oy, int oz) {
        EnumMap<Direction, String> exposed = getTextureMapping(frontAxis, ox, oy, oz);
        Identifier modelId = ModelIds.getBlockModelId(block).withSuffixedPath("_part_" + frontAxis.getName() + "_" + ox + oy + oz);
        TextureMap textures = new TextureMap();
        for (Direction direction : Direction.values()) {
            textures.put(getTextureKey(direction), textureId(exposed.getOrDefault(direction, FALLBACK_TEXTURE)));
        }
        String particle = exposed.values().stream().findFirst().orElse(FALLBACK_TEXTURE);
        textures.put(TextureKey.PARTICLE, textureId(particle));
        return CARGO_CRATE_PART_MODEL.upload(modelId, textures, generator.modelCollector);
    }

    private static TextureKey getTextureKey(Direction direction) {
        return switch (direction) {
            case DOWN -> TextureKey.DOWN;
            case UP -> TextureKey.UP;
            case NORTH -> TextureKey.NORTH;
            case SOUTH -> TextureKey.SOUTH;
            case WEST -> TextureKey.WEST;
            case EAST -> TextureKey.EAST;
        };
    }

    private static Identifier textureId(String name) {
        return NeMuelch.getId("block/" + name);
    }

    private static String buildTextureName(boolean isFrontOrBack, int rowOffset, int colOffset) {
        String sideType = isFrontOrBack ? "front" : "side";
        String row = switch (MathHelper.clamp(rowOffset, 0, 2)) {
            case 0 -> "bottom";
            case 2 -> "top";
            default -> "mid";
        };
        String col = switch (MathHelper.clamp(colOffset, 0, 2)) {
            case 0 -> "left";
            case 2 -> "right";
            default -> "mid";
        };
        return "cargo_crate_%s_%s_%s".formatted(sideType, row, col);
    }

    private static EnumMap<Direction, String> getTextureMapping(Direction.Axis frontAxis, int offsetX, int offsetY, int offsetZ) {
        EnumMap<Direction, String> exposedFaces = new EnumMap<>(Direction.class);
        if (offsetX == 0)
            exposedFaces.put(Direction.WEST, buildTextureName(frontAxis == Direction.Axis.X, offsetY, offsetZ));
        if (offsetX == 2)
            exposedFaces.put(Direction.EAST, buildTextureName(frontAxis == Direction.Axis.X, offsetY, 2 - offsetZ));
        if (offsetZ == 0)
            exposedFaces.put(Direction.NORTH, buildTextureName(frontAxis == Direction.Axis.Z, offsetY, 2 - offsetX));
        if (offsetZ == 2)
            exposedFaces.put(Direction.SOUTH, buildTextureName(frontAxis == Direction.Axis.Z, offsetY, offsetX));
        if (offsetY == 0)
            exposedFaces.put(Direction.DOWN, buildTextureName(frontAxis == Direction.Axis.Y, offsetZ, offsetX));
        if (offsetY == 2)
            exposedFaces.put(Direction.UP, buildTextureName(frontAxis == Direction.Axis.Y, offsetZ, offsetX));
        return exposedFaces;
    }
}
