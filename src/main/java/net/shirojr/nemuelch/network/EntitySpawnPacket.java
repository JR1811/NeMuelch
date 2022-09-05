package net.shirojr.nemuelch.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;

import java.util.UUID;

public class EntitySpawnPacket {

    public static final Identifier ID = new Identifier(NeMuelch.MOD_ID, "spawn_packet");

    public static Packet<?> createPacket(Entity e) {
        if (e.world.isClient)
            throw new IllegalStateException("SpawnPacketUtil.create called on the logical client!");
        PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
        byteBuf.writeVarInt(Registry.ENTITY_TYPE.getRawId(e.getType()));
        byteBuf.writeUuid(e.getUuid());
        byteBuf.writeVarInt(e.getId());

        PacketBufUtil.writeVec3d(byteBuf, e.getPos());
        PacketBufUtil.writeAngle(byteBuf, e.getPitch());
        PacketBufUtil.writeAngle(byteBuf, e.getYaw());

        return ServerPlayNetworking.createS2CPacket(ID, byteBuf);
    }

    public static final class PacketBufUtil {

        /**
         * Packs a floating-point angle into a {@code byte}.
         *
         * @param angle
         *         angle
         * @return packed angle
         */
        public static byte packAngle(float angle) {
            return (byte) MathHelper.floor(angle * 256 / 360);
        }

        /**
         * Unpacks a floating-point angle from a {@code byte}.
         *
         * @param angleByte
         *         packed angle
         * @return angle
         */
        public static float unpackAngle(byte angleByte) {
            return (angleByte * 360) / 256f;
        }

        /**
         * Writes an angle to a {@link PacketByteBuf}.
         *
         * @param byteBuf
         *         destination buffer
         * @param angle
         *         angle
         */
        public static void writeAngle(PacketByteBuf byteBuf, float angle) {
            byteBuf.writeByte(packAngle(angle));
        }

        /**
         * Reads an angle from a {@link PacketByteBuf}.
         *
         * @param byteBuf
         *         source buffer
         * @return angle
         */
        public static float readAngle(PacketByteBuf byteBuf) {
            return unpackAngle(byteBuf.readByte());
        }

        /**
         * Writes a {@link Vec3d} to a {@link PacketByteBuf}.
         *
         * @param byteBuf
         *         destination buffer
         * @param vec3d
         *         vector
         */
        public static void writeVec3d(PacketByteBuf byteBuf, Vec3d vec3d) {
            byteBuf.writeDouble(vec3d.x);
            byteBuf.writeDouble(vec3d.y);
            byteBuf.writeDouble(vec3d.z);
        }

        /**
         * Reads a {@link Vec3d} from a {@link PacketByteBuf}.
         *
         * @param byteBuf
         *         source buffer
         * @return vector
         */
        public static Vec3d readVec3d(PacketByteBuf byteBuf) {
            double x = byteBuf.readDouble();
            double y = byteBuf.readDouble();
            double z = byteBuf.readDouble();
            return new Vec3d(x, y, z);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void onPacket(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buffer, PacketSender sender) {
        EntityType<?> type = Registry.ENTITY_TYPE.get(buffer.readVarInt());
        UUID entityUUID = buffer.readUuid();
        int entityID = buffer.readVarInt();
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        float pitch = (buffer.readByte() * 360) / 256.0F;
        float yaw = (buffer.readByte() * 360) / 256.0F;
        client.execute(() -> {
            World world = client.player.getEntityWorld();
            Entity entity = type.create(world);
            if (entity != null && world.isClient) {
                entity.updatePosition(x, y, z);
                entity.updateTrackedPosition(x, y, z);
                entity.setPitch(pitch);
                entity.setYaw(yaw);
                entity.setId(entityID);
                entity.setUuid(entityUUID);
                ClientWorld clientWorld = MinecraftClient.getInstance().world;
                clientWorld.addEntity(entityID, entity);
            }
        });
    }
}
