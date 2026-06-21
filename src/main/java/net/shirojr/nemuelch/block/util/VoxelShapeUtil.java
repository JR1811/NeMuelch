package net.shirojr.nemuelch.block.util;

import net.minecraft.block.Block;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class VoxelShapeUtil {
    @SuppressWarnings("unused")
    public static VoxelShape createRotatedShape(int[] points, Direction direction) {
        return switch (direction) {
            case NORTH -> Block.createCuboidShape(
                    points[0], points[1], points[2],
                    points[3], points[4], points[5]
            );
            case SOUTH -> Block.createCuboidShape(
                    16 - points[3], points[1], 16 - points[5],
                    16 - points[0], points[4], 16 - points[2]
            );
            case WEST -> Block.createCuboidShape(
                    points[2], points[1], 16 - points[3],
                    points[5], points[4], 16 - points[0]
            );
            case EAST -> Block.createCuboidShape(
                    16 - points[5], points[1], points[0],
                    16 - points[2], points[4], points[3]
            );
            case DOWN -> Block.createCuboidShape(
                    points[0], points[2], points[1],
                    points[3], points[5], points[4]
            );
            case UP -> Block.createCuboidShape(points[0], 16 - points[5], 16 - points[4],
                    points[3], 16 - points[2], 16 - points[1]
            );
        };
    }

    public static VoxelShape createRotatedShape(int[] points, WallMountLocation face, Direction facing) {
        return switch (face) {
            case FLOOR -> rotateHorizontal(points, facing);
            case CEILING -> rotateHorizontal(flipVertical(points), facing);
            case WALL -> rotateWall(points, facing);
        };
    }

    private static VoxelShape rotateHorizontal(int[] points, Direction facing) {
        return switch (facing) {
            case SOUTH -> Block.createCuboidShape(
                    16 - points[3], points[1], 16 - points[5],
                    16 - points[0], points[4], 16 - points[2]
            );
            case WEST -> Block.createCuboidShape(
                    points[2], points[1], 16 - points[3],
                    points[5], points[4], 16 - points[0]
            );
            case EAST -> Block.createCuboidShape(
                    16 - points[5], points[1], points[0],
                    16 - points[2], points[4], points[3]
            );
            default -> Block.createCuboidShape(
                    points[0], points[1], points[2],
                    points[3], points[4], points[5]
            );
        };
    }

    private static VoxelShape rotateWall(int[] points, Direction facing) {
        return switch (facing) {
            case NORTH -> Block.createCuboidShape(
                    points[0], points[2], 16 - points[4],
                    points[3], points[5], 16 - points[1]
            );
            case EAST -> Block.createCuboidShape(
                    points[1], points[2], points[0],
                    points[4], points[5], points[3]
            );
            case WEST -> Block.createCuboidShape(
                    16 - points[4], points[2], points[0],
                    16 - points[1], points[5], points[3]
            );
            default -> Block.createCuboidShape(
                    points[0], points[2], points[1],
                    points[3], points[5], points[4]
            );
        };
    }

    private static int[] flipVertical(int[] points) {
        return new int[]{points[0], 16 - points[4], points[2], points[3], 16 - points[1], points[5]};
    }
}
