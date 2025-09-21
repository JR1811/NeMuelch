package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;

import java.util.EnumSet;
import java.util.Optional;

public interface BlightChunkComponent extends Component {
    Identifier KEY = NeMuelch.getId("blight");

    default Optional<BlightChunkComponent> get(Chunk chunk) {
        return NeMuelchComponents.BLIGHT.maybeGet(chunk);
    }

    Chunk getProvider();


    default void sync() {
        NeMuelchComponents.BLIGHT.sync(getProvider());
    }

    enum BlightType {
        WITHERING,      // Crops grow slower, reduced yield
        POISONOUS,      // Crops become harmful when eaten
        CORRUPTED,      // Crops turn into different/dangerous items
        SPREADING;      // Actively spreads to nearby chunks

        private static final BlightType[] VALUES = values();
        public int getFlag() {
            return 1 << this.ordinal(); // 2^ordinal bit shift for flags
        }

        public static int getFlags(EnumSet<BlightType> types) {
            int flags = 0;
            for (BlightType type : types) {
                flags |= type.getFlag();
            }
            return flags;
        }

        public EnumSet<BlightType> fromFlags(int flags) {
            EnumSet<BlightType> result = EnumSet.noneOf(BlightType.class);
            for (BlightType type : VALUES) {
                if ((flags & type.getFlag()) == 0) continue;
                result.add(type);
            }
            return result;
        }

        public static boolean hasAnyType(int flags) {
            return flags != 0;
        }

        public static int getTypesAmount(int flags) {
            return Integer.bitCount(flags);
        }
    }
}
