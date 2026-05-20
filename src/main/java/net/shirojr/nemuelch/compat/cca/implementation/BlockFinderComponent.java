package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.network.packet.BlockFinderActiveS2CPacket;
import net.shirojr.nemuelch.network.packet.BlockFinderResultS2CPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.function.Predicate;

public class BlockFinderComponent implements Component, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("block_finder");

    public static final int MAX_RANGE = 50;
    public static final Predicate<CachedBlockPosition> EMPTY_SEARCH_CRITERIA = entry -> false;
    public static final Predicate<CachedBlockPosition> STORAGE_SEARCH_CRITERIA = entry -> entry.getBlockEntity() instanceof Inventory;
    public static final Predicate<CachedBlockPosition> NON_EMPTY_STORAGE_SEARCH_CRITERIA = entry ->
            entry.getBlockEntity() instanceof Inventory inventory && !inventory.isEmpty();

    private boolean active;
    private final PlayerEntity holder;
    private Predicate<CachedBlockPosition> searchCriteria;
    private int radius;
    private HashSet<BlockPos> oldResult;

    public BlockFinderComponent(PlayerEntity holder) {
        this.holder = holder;
        this.active = false;
        this.searchCriteria = EMPTY_SEARCH_CRITERIA;
        this.radius = 20;
        this.oldResult = new HashSet<>();
    }

    public static BlockFinderComponent get(ServerPlayerEntity player) {
        return NeMuelchComponents.BLOCK_FINDER.get(player);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        boolean oldActive = this.isActive();
        this.active = active;
        if (oldActive == active) return;
        if (this.holder instanceof ServerPlayerEntity serverPlayer) {
            new BlockFinderActiveS2CPacket(this.active).send(serverPlayer);
        }
        if (!this.active) {
            this.oldResult.clear();
        }
    }

    public Predicate<CachedBlockPosition> getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(Predicate<CachedBlockPosition> searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.min(radius, MAX_RANGE);
    }

    @Override
    public void serverTick() {
        if (!this.isActive()) return;
        if (!(holder instanceof ServerPlayerEntity serverPlayer)) return;
        if (this.getRadius() == 0) return;
        int interval = serverPlayer.getServerWorld().getGameRules().getInt(NemuelchGameRules.BLOCK_FINDER_INTERVAL);
        if (this.holder.age % interval != 0) return;
        if (this.getSearchCriteria().equals(EMPTY_SEARCH_CRITERIA)) {
            this.setActive(false);
            return;
        }

        HashSet<BlockPos> searchResult = getSearchResult(serverPlayer.getServerWorld(), holder.getBlockPos());
        if (searchResult.equals(this.oldResult)) return;
        new BlockFinderResultS2CPacket(searchResult).send(serverPlayer);
        this.oldResult = searchResult;
    }

    private HashSet<BlockPos> getSearchResult(ServerWorld world, BlockPos origin) {
        HashSet<BlockPos> result = new HashSet<>();
        int chunkRadius = (this.getRadius() >> 4) + 1;
        ChunkPos centerChunk = new ChunkPos(origin);

        for (int chunkX = -chunkRadius; chunkX <= chunkRadius; chunkX++) {
            for (int chunkZ = -chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
                int currentChunkX = centerChunk.x + chunkX;
                int currentChunkZ = centerChunk.z + chunkZ;
                WorldChunk chunk = world.getChunkManager().getWorldChunk(currentChunkX, currentChunkZ);
                if (chunk == null) continue;
                ChunkSection[] sections = chunk.getSectionArray();
                for (int sectionY = 0; sectionY < sections.length; sectionY++) {
                    ChunkSection section = sections[sectionY];
                    if (section == null || section.isEmpty()) continue;
                    int bottomY = (world.getBottomSectionCoord() + sectionY) << 4;
                    int topY = bottomY + 15;
                    if (topY < origin.getY() - this.getRadius() || bottomY > origin.getY() + this.getRadius()) continue;
                    this.searchChunkSection(result, world, origin, currentChunkX, currentChunkZ, bottomY);
                }
            }
        }
        return result;
    }

    private void searchChunkSection(HashSet<BlockPos> result, ServerWorld world, BlockPos origin, int currentChunkX, int currentChunkZ, int sectionBottomY) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos worldPos = new BlockPos(currentChunkX * 16 + x, sectionBottomY + y, currentChunkZ * 16 + z);
                    if (!worldPos.isWithinDistance(origin, this.getRadius())) continue;
                    CachedBlockPosition cachedPos = new CachedBlockPosition(world, worldPos, true);
                    if (this.searchCriteria.test(cachedPos)) {
                        result.add(worldPos);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static void onRespawn(BlockFinderComponent oldComponent, BlockFinderComponent newComponent,
                                 boolean lossless, boolean keepInventory, boolean sameCharacter) {
        newComponent.setActive(oldComponent.isActive());
        newComponent.setSearchCriteria(oldComponent.getSearchCriteria());
        newComponent.setRadius(oldComponent.getRadius());
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains("IsActive")) {
            this.active = nbt.getBoolean("IsActive");
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        nbt.putBoolean("IsActive", this.isActive());
    }
}
