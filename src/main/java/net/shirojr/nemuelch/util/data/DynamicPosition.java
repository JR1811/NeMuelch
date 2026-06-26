package net.shirojr.nemuelch.util.data;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import net.shirojr.nemuelch.util.helper.NbtUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class DynamicPosition {
    private Vec3d staticPos;
    private UUID dynamicPosUuid;
    private final Set<StateChangedCallback> listeners;

    public DynamicPosition(@Nullable Vec3d staticPos, @Nullable UUID dynamicPosUuid) {
        this.staticPos = staticPos;
        this.dynamicPosUuid = dynamicPosUuid;
        this.listeners = new HashSet<>();
        this.stateTest();
    }

    public Vec3d getStaticPos() {
        return staticPos;
    }

    public void setStaticPos(@Nullable Vec3d staticPos) {
        Vec3d old = this.staticPos;
        this.staticPos = staticPos;
        this.stateTest();
        if (old == null || !old.equals(this.staticPos)) {
            this.listeners.forEach(callback -> callback.onStateChanged(this));
        }
    }

    public UUID getDynamicPosUuid() {
        return dynamicPosUuid;
    }

    @Nullable
    public Entity getDynamicPosEntity(ServerWorld world) {
        if (this.getDynamicPosUuid() == null) return null;
        return world.getEntity(this.getDynamicPosUuid());
    }

    public void setDynamicPosUuid(@Nullable UUID dynamicPosUuid) {
        UUID old = this.dynamicPosUuid;
        this.dynamicPosUuid = dynamicPosUuid;
        this.stateTest();
        if (old == null || !old.equals(this.dynamicPosUuid)) {
            this.listeners.forEach(callback -> callback.onStateChanged(this));
        }
    }

    public Vec3d getPos(ServerWorld world) {
        Entity dynamicPosEntity = this.getDynamicPosEntity(world);
        if (dynamicPosEntity != null) return dynamicPosEntity.getPos();
        return this.getStaticPos();
    }

    public static DynamicPosition fromNbt(NbtCompound nbt) {
        if (!nbt.contains(NbtKeys.DYNAMIC_POS_HANDLER)) {
            throw new NullPointerException("No Dynamic Pos found: " + nbt);
        }
        NbtCompound dynamicPosHandlerNbt = nbt.getCompound(NbtKeys.DYNAMIC_POS_HANDLER);
        Vec3d staticPos = null;
        UUID dynamicPosEntityUuid = null;

        if (dynamicPosHandlerNbt.contains(NbtKeys.STATIC_POS)) {
            staticPos = NbtUtil.vec3dFromNbt(dynamicPosHandlerNbt, NbtKeys.STATIC_POS);
        }
        if (dynamicPosHandlerNbt.contains(NbtKeys.DYNAMIC_POS)){
            dynamicPosEntityUuid = dynamicPosHandlerNbt.getUuid(NbtKeys.DYNAMIC_POS);
        }
        return new DynamicPosition(staticPos, dynamicPosEntityUuid);
    }

    public void toNbt(NbtCompound nbt) {
        NbtCompound dynamicPosHandlerNbt = new NbtCompound();
        if (this.getStaticPos() != null) {
            NbtUtil.vec3dToNbt(dynamicPosHandlerNbt, NbtKeys.STATIC_POS, this.getStaticPos());
        } else if (this.dynamicPosUuid != null) {
            dynamicPosHandlerNbt.putUuid(NbtKeys.DYNAMIC_POS, this.getDynamicPosUuid());
        }
        nbt.put(NbtKeys.DYNAMIC_POS_HANDLER, dynamicPosHandlerNbt);
    }

    public void registerListener(StateChangedCallback listener) {
        this.listeners.add(listener);
    }

    private void stateTest() {
        if (staticPos == null && dynamicPosUuid == null) {
            throw new IllegalStateException("Dynamic Position didn't conform to requirements.");
        }
    }

    public interface StateChangedCallback {
        void onStateChanged(DynamicPosition position);
    }
}
