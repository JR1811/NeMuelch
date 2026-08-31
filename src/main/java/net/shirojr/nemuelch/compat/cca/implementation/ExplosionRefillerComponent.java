package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.BlockCollectionEntry;
import net.shirojr.nemuelch.compat.cca.util.BlockSnapshot;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.function.BiPredicate;

public class ExplosionRefillerComponent implements Component, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("explosion_refiller");
    private static final int TICK_INTERVAL = 20;
    private static final int BLOCKS_PER_ACTION = 3;
    private static final int CRATER_START_FILLING_DELAY = 20 * 5;
    public static final int MAX_BACKLOG_SIZE = 500;
    private static final double PLAYER_NEARBY_DISTANCE = 5;
    public static final BiPredicate<ServerWorld, BlockPos> CAN_REPLACE = (serverWorld, pos) -> {
        BlockState state = serverWorld.getBlockState(pos);
        return state.isAir() || state.getBlock() instanceof FluidBlock;
    };
    private final World world;
    private final ArrayDeque<BlockCollectionEntry> queue = new ArrayDeque<>();
    private int tick = 0;

    public ExplosionRefillerComponent(World world) {
        this.world = world;
    }

    public static ExplosionRefillerComponent get(World world) {
        return NeMuelchComponents.EXPLOSION_REFILLER.get(world);
    }

    public void addEntry(BlockCollectionEntry entry) {
        if (entry.blocks().isEmpty()) return;
        if (this.queue.size() >= MAX_BACKLOG_SIZE) {
            this.queue.pollFirst();
            NeMuelch.LOGGER.warn("Explosion refilling entries backlog exceeded safety size ({}). Dropped last entry", MAX_BACKLOG_SIZE);
        }
        this.queue.addLast(entry);
    }

    public void clear() {
        this.queue.clear();
    }

    public int size() {
        return this.queue.size();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    private static boolean playerNearby(ServerWorld world, Vec3d pos) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            double distance = ExplosionRefillerComponent.PLAYER_NEARBY_DISTANCE;
            if (player.squaredDistanceTo(pos) <= distance * distance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serverTick() {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (++this.tick < TICK_INTERVAL) return;
        this.tick = 0;
        int budget = BLOCKS_PER_ACTION;
        int attempts = this.size();
        while (budget > 0 && attempts-- > 0) {
            BlockCollectionEntry entry = queue.pollFirst();
            if (entry == null) break;
            if (this.world.getTime() - entry.creationTime() < CRATER_START_FILLING_DELAY) {
                queue.addLast(entry);
                continue;
            }
            ObjectArrayList<BlockSnapshot> blocks = entry.blocks();
            if (!blocks.isEmpty()) {
                int index = blocks.size() - 1;
                BlockSnapshot blockSnapshot = blocks.get(index);
                BlockPos entryPos = blockSnapshot.pos();
                ChunkPos chunkPos = new ChunkPos(entryPos);
                if (this.world.isChunkLoaded(chunkPos.x, chunkPos.z) && !playerNearby(serverWorld, entryPos.toCenterPos())) {
                    blocks.remove(index);
                    if (CAN_REPLACE.test(serverWorld, entryPos)) {
                        BlockState state = blockSnapshot.state();
                        serverWorld.setBlockState(entryPos, state, Block.NOTIFY_LISTENERS);
                        BlockSoundGroup soundGroup = state.getSoundGroup();
                        serverWorld.playSound(null, entryPos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS);
                    }
                    budget--;
                }
            }
            if (!blocks.isEmpty()) {
                this.queue.addLast(entry);
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.queue.clear();
        RegistryWrapper.Impl<Block> blockRegistry = this.world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BLOCK);
        if (tag.contains(NeMuelchNbtKeys.QUEUE)) {
            NbtList queueNbt = tag.getList(NeMuelchNbtKeys.QUEUE, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < queueNbt.size(); i++) {
                NbtCompound entryNbt = queueNbt.getCompound(i);
                long time = entryNbt.getLong(NeMuelchNbtKeys.TIME);
                NbtList blocksNbt = entryNbt.getList(NeMuelchNbtKeys.BLOCKS, NbtElement.COMPOUND_TYPE);

                ObjectArrayList<BlockSnapshot> blocks = new ObjectArrayList<>(blocksNbt.size());
                for (int j = 0; j < blocksNbt.size(); j++) {
                    NbtCompound blockNbt = blocksNbt.getCompound(j);
                    BlockPos pos = BlockPos.fromLong(blockNbt.getLong(NeMuelchNbtKeys.POS));
                    BlockState state = NbtHelper.toBlockState(blockRegistry, blockNbt.getCompound(NeMuelchNbtKeys.STATE));
                    blocks.add(new BlockSnapshot(pos, state));
                }
                this.queue.addLast(new BlockCollectionEntry(time, blocks));
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList queueNbt = new NbtList();
        for (BlockCollectionEntry entry : this.queue) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putLong(NeMuelchNbtKeys.TIME, entry.creationTime());
            NbtList blocksNbt = new NbtList();
            for (BlockSnapshot block : entry.blocks()) {
                NbtCompound blockNbt = new NbtCompound();
                blockNbt.putLong(NeMuelchNbtKeys.POS, block.pos().asLong());
                blockNbt.put(NeMuelchNbtKeys.STATE, NbtHelper.fromBlockState(block.state()));
                blocksNbt.add(blockNbt);
            }
            entryNbt.put(NeMuelchNbtKeys.BLOCKS, blocksNbt);
            queueNbt.add(entryNbt);
        }
        tag.put(NeMuelchNbtKeys.QUEUE, queueNbt);
    }
}
