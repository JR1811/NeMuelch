package net.shirojr.nemuelch.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
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

    public static final TrackedDataHandler<Optional<Vec3d>> OPTIONAL_POS = new TrackedDataHandler<>() {
        public void write(PacketByteBuf buf, Optional<Vec3d> optPos) {
            if (optPos.isPresent()) {
                buf.writeBoolean(true);
                buf.writeDouble(optPos.get().getX());
                buf.writeDouble(optPos.get().getY());
                buf.writeDouble(optPos.get().getZ());
            } else {
                buf.writeBoolean(false);
            }
        }

        public Optional<Vec3d> read(PacketByteBuf buf) {
            return buf.readBoolean() ? Optional.of(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble())) : Optional.empty();
        }

        public Optional<Vec3d> copy(Optional<Vec3d> optPos) {
            return optPos;
        }
    };

    public static final TrackedDataHandler<Double> DOUBLE = new TrackedDataHandler<>() {
        public void write(PacketByteBuf packetByteBuf, Double value) {
            packetByteBuf.writeDouble(value);
        }

        public Double read(PacketByteBuf packetByteBuf) {
            return packetByteBuf.readDouble();
        }

        public Double copy(Double value) {
            return value;
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
        TrackedDataHandlerRegistry.register(OPTIONAL_POS);
    }
}
