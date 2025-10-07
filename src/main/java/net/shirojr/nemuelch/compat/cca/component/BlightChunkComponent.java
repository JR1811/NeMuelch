package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Use {@link #maybeGet(Chunk)} to get access to the Chunk Blight data
 */
public interface BlightChunkComponent extends Component, ServerTickingComponent, AutoSyncedComponent {
    Identifier KEY = NeMuelch.getId("blight");

    Map<BlockState, Boolean> BLIGHT_IMMUNITY_CACHE = new WeakHashMap<>();
    Predicate<BlockState> NO_BLIGHT = state -> BLIGHT_IMMUNITY_CACHE.computeIfAbsent(state, entry -> {
        if (!entry.getFluidState().isEmpty()) return true;
        if (state.isIn(NeMuelchTags.Blocks.NEVER_BLIGHT)) return true;
        return state.isIn(BlockTags.PICKAXE_MINEABLE);
    });

    static Optional<BlightChunkComponent> maybeGet(@Nullable Chunk chunk) {
        if (chunk == null) return Optional.empty();
        return NeMuelchComponents.BLIGHT.maybeGet(chunk);
    }

    @Nullable
    default ServerWorld getServerWorld() {
        if (!(getProvider() instanceof WorldChunk worldChunk)) return null;
        if (!(worldChunk.getWorld() instanceof ServerWorld serverWorld)) return null;
        return serverWorld;
    }

    Chunk getProvider();

    /**
     * @return returns all assigned block specific {@link BlightType BlightTypes} including {@link #getCompleteChunkBlights()}
     */
    EnumSet<BlightType> getBlightsOfPos(BlockPos pos);

    /**
     * @return Blocks with requested {@link BlightType BlightTypes}. This specifically excludes blights which are
     * marked for the whole chunk {@link #getCompleteChunkBlights()}
     */
    HashSet<BlockPos> getPosWithBlights(BlightType... types);

    void addBlightsToPos(BlockPos pos, Set<BlightType> types);

    EnumSet<BlightType> getCompleteChunkBlights();


    /**
     * Defines the amount of Blocks which a {@link BlightType} needs to occupy to mark the
     * chunk as fully blighted of that type
     *
     * @return normalized (0-1) amount of Blocks to where the chunk gets completely marked of that {@link BlightType}.
     * @implNote Use low values (~ between 0.5% and 10%), as the full chunk (even air) is being considered
     */
    double getCompleteBlightThreshold();

    void setCompleteBlightThreshold(double normalizedValue);

    /**
     * @return World time of when the chunk went from zero blights to one or more blights. Upon, and as long as being cleared of any Blights
     * this will be represented as `-1`
     */
    long getTimeOfFirstInitializedBlight();

    void clearAndConvertToCompleteBlight(BlightType type);

    /**
     * @return `-1` if it's contained in the {@link #getCompleteChunkBlights()} data of the chunk
     */
    int getBlightPosCount(BlightType type);

    /**
     * @param types If none are specified, it will check if it has any blight type associated
     */
    default boolean isBlighted(BlockPos pos, BlightType... types) {
        EnumSet<BlightType> blightsOfPos = getBlightsOfPos(pos);
        if (types.length == 0) {
            return !blightsOfPos.isEmpty();
        }
        for (BlightType type : types) {
            if (!blightsOfPos.contains(type)) return false;
        }
        return true;
    }

    default boolean isBlightedWithAllTypes(BlockPos pos) {
        return getBlightsOfPos(pos).size() >= BlightType.CACHED_VALUES.length;
    }

    /**
     * @param types if 0 types are specified it will check for all existing {@link BlightType BlightTypes}
     */
    boolean isChunkCompletelyBlighted(BlightType... types);

    /**
     * @return true, if either complete or partial blight data contains specified types
     */
    boolean contains(BlightType... types);

    void clear(boolean blights, boolean completeBlights, boolean markDirty);

    void clear(BlightType... types);

    /**
     * @param types if empty, clears all {@link BlightType BlightTypes} of pos
     */
    void clearPos(BlockPos pos, Set<BlightType> types);

    boolean isEmpty();

    void markDirty();

    default void sync() {
        NeMuelchComponents.BLIGHT.sync(getProvider());
    }

    static double getNormalizedPortionOfChunk(Chunk chunk, int blocks) {
        int maxChunkBlockCount = chunk.getHeight() * 16 * 16;
        return MathHelper.clamp(blocks, 0d, maxChunkBlockCount) / maxChunkBlockCount;
    }

    /*default boolean isBlightImmune(BlockState state, Set<BlightType> types) {
        if (state.isAir() && !types.contains(BlightType.AIRBORNE)) return true;
        return NO_BLIGHT.test(state);
    }*/
}
