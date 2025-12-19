package net.shirojr.nemuelch.block.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class AdvancedFogBlockEntity extends BlockEntity {
    @NotNull
    private Data data;

    public AdvancedFogBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.ADVANCED_FOG, pos, state);
        this.data = new Data();
    }

    public @NotNull Data getData() {
        return data;
    }

    public void setData(@NotNull Data data, boolean markDirty) {
        this.data = data;
        if (markDirty) markDirty();
    }

    public Box getRenderedFaces() {
        return this.data.box;
    }

    public Vector4f getColor() {
        return this.data.color;
    }

    public float getRed() {
        return this.data.color.x;
    }

    public float getGreen() {
        return this.data.color.y;
    }

    public float getBlue() {
        return this.data.color.z;
    }

    public float getAlpha() {
        return this.data.color.w;
    }

    @Override
    public void markDirty() {
        if (this.world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity target : PlayerLookup.all(serverWorld.getServer())) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeLong(this.getPos().asLong());
                this.data.toPacketByteBuf(buf);
                ServerPlayNetworking.send(target, NetworkIdentifiers.ADVANCED_FOG_SYNC, buf);
            }
        }
        super.markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, AdvancedFogBlockEntity blockEntity) {

    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setData(Data.fromNbt(nbt), false);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.data.toNbt(nbt);
    }


    // ==========================================================================
    //                     Internal Data Handling
    // ==========================================================================

    public record Data(Box box, Vector4f color) {
        public Data() {
            this(new Box(0, 0, 0, 1, 1, 1), new Vector4f(0.2f, 0.8f, 0.2f, 0.6f));
        }

        public Data withColor(Vector4f newColor) {
            return new Data(box, newColor);
        }

        public Data withBox(Box newBox) {
            return new Data(newBox, color);
        }

        public void toPacketByteBuf(PacketByteBuf buf) {
            buf.writeDouble(this.box.minX);
            buf.writeDouble(this.box.minY);
            buf.writeDouble(this.box.minZ);
            buf.writeDouble(this.box.maxX);
            buf.writeDouble(this.box.maxY);
            buf.writeDouble(this.box.maxZ);

            buf.writeFloat(this.color.x);
            buf.writeFloat(this.color.y);
            buf.writeFloat(this.color.z);
            buf.writeFloat(this.color.w);
        }

        public static Data fromPacketByteBuf(PacketByteBuf buf) {
            Box box = new Box(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()
            );
            Vector4f color = new Vector4f(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat()
            );
            return new Data(box, color);
        }

        public void toNbt(NbtCompound nbt) {
            NbtCompound boxNbt = new NbtCompound();
            boxNbt.putDouble("minX", this.box.minX);
            boxNbt.putDouble("minY", this.box.minY);
            boxNbt.putDouble("minZ", this.box.minZ);
            boxNbt.putDouble("maxX", this.box.maxX);
            boxNbt.putDouble("maxY", this.box.maxY);
            boxNbt.putDouble("maxZ", this.box.maxZ);
            nbt.put("box", boxNbt);

            NbtCompound colorNbt = new NbtCompound();
            colorNbt.putFloat("r", color.x);
            colorNbt.putFloat("g", color.y);
            colorNbt.putFloat("b", color.z);
            colorNbt.putFloat("a", color.w);
            nbt.put("color", colorNbt);
        }

        public static Data fromNbt(NbtCompound nbt) {
            NbtCompound boxNbt = nbt.getCompound("box");
            Box box = new Box(
                    boxNbt.getDouble("minX"), boxNbt.getDouble("minY"), boxNbt.getDouble("minZ"),
                    boxNbt.getDouble("maxX"), boxNbt.getDouble("maxY"), boxNbt.getDouble("maxZ")
            );

            NbtCompound colorNbt = nbt.getCompound("color");
            Vector4f color = new Vector4f(
                    colorNbt.getFloat("r"), colorNbt.getFloat("g"), colorNbt.getFloat("b"), colorNbt.getFloat("a")
            );
            return new Data(box, color);
        }
    }
}
