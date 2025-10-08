package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BlightSpreader {
    public static final int BORDER_SPREAD_ATTEMPTS = 4;

    private final BlightChunkComponent component;

    public BlightSpreader(BlightChunkComponent component) {
        this.component = component;
    }

    public void spreadFromPartialChunk(ServerWorld world) {
        for (BlockPos spreaderPos : component.getPosWithBlights(BlightType.SPREADING)) {
            spreadPartial(world, spreaderPos, component.getBlightsOfPos(spreaderPos));
        }
    }

    public void spreadFromCompleteChunk(ServerWorld world) {
        Random random = world.getRandom();
        ChunkPos chunkPos = component.getProvider().getPos();

        int minX = chunkPos.getStartX();
        int maxX = chunkPos.getEndX();
        int minZ = chunkPos.getStartZ();
        int maxZ = chunkPos.getEndZ();

        for (int i = 0; i < BORDER_SPREAD_ATTEMPTS; i++) {
            BlockPos borderPos = getRandomBorderPosition(random, minX, maxX, minZ, maxZ, world);

            if (random.nextFloat() < 0.3f) {
                EnumSet<BlightType> blightsOfPos = component.getBlightsOfPos(borderPos);
                spreadCompleteChunk(world, borderPos, blightsOfPos);
            }
        }
    }

    public BlockPos getRandomBorderPosition(Random random, int minX, int maxX, int minZ, int maxZ, ServerWorld world) {
        Direction direction = Direction.Type.HORIZONTAL.random(random);
        int x, z;

        switch (direction) {
            case NORTH -> {
                x = minX + random.nextInt(16);
                z = minZ;
            }
            case EAST -> {
                x = maxX;
                z = minZ + random.nextInt(16);
            }
            case SOUTH -> {
                x = minX + random.nextInt(16);
                z = maxZ;
            }
            default -> {
                x = minX;
                z = minZ + random.nextInt(16);
            }
        }
        return new BlockPos(x, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z), z);
    }

    public void spreadPartial(ServerWorld world, BlockPos pos, Set<BlightType> blights) {
        Random random = world.getRandom();
        HashSet<BlockPos> appliedTargetPos = new HashSet<>();
        for (Direction direction : Direction.values()) {
            if (random.nextFloat() >= 0.8) continue;
            BlockPos neighborPos = pos.offset(direction);
            if (!world.getChunk(neighborPos).equals(component.getProvider())) continue;

            Chunk targetChunk = world.getChunk(neighborPos);
            Optional<BlightChunkComponent> neighborComponent = BlightChunkComponent.maybeGet(
                    world.getChunk(targetChunk.getPos().x, targetChunk.getPos().z, ChunkStatus.FULL, false)
            );
            neighborComponent.ifPresent(otherComponent -> {
                otherComponent.addBlightsToPos(neighborPos, blights);
                appliedTargetPos.add(neighborPos);
            });
        }
        boolean clearedSource = false;
        if (random.nextFloat() < 0.05) {
            this.component.clearPos(pos, Set.of());
            clearedSource = true;
        }
        for (BlockPos appliedTo : appliedTargetPos) {
            for (BlightType blight : blights) {
                blight.getActions().get().onSuccessfulSpread(
                        world, this.component.getTimeOfFirstInitializedBlight(), pos, appliedTo, clearedSource
                );
            }
        }
    }

    private void spreadCompleteChunk(ServerWorld world, BlockPos borderPos, EnumSet<BlightType> blights) {
        Random random = world.getRandom();
        HashSet<BlockPos> appliedTargetPos = new HashSet<>();
        for (Direction direction : Direction.values()) {
            if (random.nextFloat() >= 0.8) continue;
            BlockPos neighborPos = borderPos.offset(direction);

            Chunk targetChunk = world.getChunk(neighborPos);
            Optional<BlightChunkComponent> neighborComponent = BlightChunkComponent.maybeGet(
                    world.getChunk(targetChunk.getPos().x, targetChunk.getPos().z, ChunkStatus.FULL, false)
            );
            neighborComponent.ifPresent(otherComponent -> {
                otherComponent.addBlightsToPos(neighborPos, blights);
                appliedTargetPos.add(neighborPos);
            });
        }
        for (BlockPos appliedTo : appliedTargetPos) {
            for (BlightType blight : blights) {
                blight.getActions().get().onSuccessfulSpread(
                        world, this.component.getTimeOfFirstInitializedBlight(), borderPos, appliedTo, false
                );
            }
        }
        if (!appliedTargetPos.isEmpty()) {
            component.setTick(-1);
        }
    }
}
