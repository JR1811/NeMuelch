package net.shirojr.nemuelch.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NeMuelchTrackedData {
    public static final TrackedDataHandler<List<Integer>> ENTITY_LIST = new TrackedDataHandler<>() {
        public void write(PacketByteBuf packetByteBuf, List<Integer> list) {
            packetByteBuf.writeVarInt(list.size());
            for (Integer entity : list) {
                packetByteBuf.writeVarInt(entity);
            }
        }

        public List<Integer> read(PacketByteBuf packetByteBuf) {
            List<Integer> list = new ArrayList<>();

            int size = packetByteBuf.readVarInt();
            for (int i = 0; i < size; i++) {
                list.add(packetByteBuf.readVarInt());
            }
            return list;
        }

        public List<Integer> copy(List<Integer> list) {
            return new ArrayList<>(list);
        }
    };

    public static final TrackedDataHandler<Optional<BlockPos>> OPTIONAL_BLOCKPOS = new TrackedDataHandler<>() {
        public void write(PacketByteBuf packetByteBuf, Optional<BlockPos> optPos) {
            if (optPos.isPresent()) {
                packetByteBuf.writeBoolean(true);
                packetByteBuf.writeLong(optPos.get().asLong());
            } else {
                packetByteBuf.writeBoolean(false);
            }
        }

        public Optional<BlockPos> read(PacketByteBuf packetByteBuf) {
            return packetByteBuf.readBoolean() ? Optional.of(BlockPos.fromLong(packetByteBuf.readLong())) : Optional.empty();
        }

        public Optional<BlockPos> copy(Optional<BlockPos> optPos) {
            return optPos;
        }
    };

    public static List<Entity> resolveEntityIds(World world, List<Integer> entityIds) {
        List<Entity> entities = new ArrayList<>();
        for (int entry : entityIds) {
            entities.add(world.getEntityById(entry));
        }
        return entities;
    }

    public static void initialize() {
        TrackedDataHandlerRegistry.register(ENTITY_LIST);
        TrackedDataHandlerRegistry.register(OPTIONAL_BLOCKPOS);
    }
}
