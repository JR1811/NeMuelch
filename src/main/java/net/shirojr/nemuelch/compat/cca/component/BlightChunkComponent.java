package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

import java.util.EnumSet;
import java.util.Optional;

public interface BlightChunkComponent extends Component {
    Identifier KEY = NeMuelch.getId("blight");

    static Optional<BlightChunkComponent> maybeGet(Chunk chunk) {
        return NeMuelchComponents.BLIGHT.maybeGet(chunk);
    }

    Chunk getProvider();

    EnumSet<BlightType> getBlightsOfPos(BlockPos pos);

    void setBlightsOnPos(BlockPos pos, BlightType... types);

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

    void clearAndConvertToCompleteBlight(BlightType type);

    /**
     * @return `-1` if it's contained in the complete {@link BlightType} data of the chunk.
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

    void clear(boolean blights, boolean completeBlights);

    boolean isEmpty();

    default void sync() {
        NeMuelchComponents.BLIGHT.sync(getProvider());
    }

    static double getNormalizedPortionOfChunk(Chunk chunk, int blocks) {
        int maxChunkBlockCount = chunk.getHeight() * 16 * 16;
        return MathHelper.clamp(blocks, 0d, maxChunkBlockCount) / maxChunkBlockCount;
    }
}
