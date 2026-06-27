package net.shirojr.nemuelch.util.data;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import net.shirojr.nemuelch.util.helper.NbtUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Holds {@link Vec3d} positions either dynamically by referencing Entities or by using static position fallbacks
 */
@SuppressWarnings("unused")
public class DynamicPosition {
    private final Set<StateChangedCallback> listeners;

    private Vec3d staticPos;
    private UUID dynamicPosUuid;
    @Nullable
    private Entity dynamicPosEntityCache;
    private final boolean afterRemovalHoldStatic;

    /**
     *
     * @param staticPos              fallback entry
     * @param dynamicPosUuid         dynamic entry
     * @param afterRemovalHoldStatic if, after removal, the entity leaves its last known position as a static fallback entry
     */
    public DynamicPosition(@Nullable Vec3d staticPos, @Nullable UUID dynamicPosUuid, boolean afterRemovalHoldStatic) {
        this.staticPos = staticPos;
        this.dynamicPosUuid = dynamicPosUuid;
        this.clearEntityCache();
        this.afterRemovalHoldStatic = afterRemovalHoldStatic;

        this.listeners = new HashSet<>();
        this.stateTest();
    }

    public DynamicPosition(@Nullable Vec3d staticPos, @Nullable UUID dynamicPosUuid) {
        this(staticPos, dynamicPosUuid, false);
    }

    public Vec3d getStaticPos() {
        return staticPos;
    }

    public void setStaticPos(@Nullable Vec3d staticPos) {
        Vec3d old = this.staticPos;
        this.staticPos = staticPos;
        this.stateTest();
        if (!Objects.equals(old, this.staticPos)) {
            this.listeners.forEach(callback -> callback.onDynamicPositionChanged(this));
        }
    }

    public UUID getDynamicPosUuid() {
        return dynamicPosUuid;
    }

    @Nullable
    public Entity getDynamicPosEntity(ServerWorld world) {
        if (this.getDynamicPosUuid() == null) return null;
        if (this.dynamicPosEntityCache != null) {
            if (!this.dynamicPosEntityCache.isRemoved()) {
                return dynamicPosEntityCache;
            }
            if (this.afterRemovalHoldStatic) {
                this.setStaticPos(dynamicPosEntityCache.getPos());
            }
            this.clearEntityCache();
            return null;
        }
        Entity retrievedEntity = world.getEntity(this.getDynamicPosUuid());
        this.dynamicPosEntityCache = retrievedEntity;
        return retrievedEntity;
    }

    public void setDynamicPosUuid(@Nullable UUID dynamicPosUuid) {
        UUID old = this.dynamicPosUuid;
        this.dynamicPosUuid = dynamicPosUuid;
        this.stateTest();
        if (!Objects.equals(old, this.dynamicPosUuid)) {
            this.clearEntityCache();
            this.listeners.forEach(callback -> callback.onDynamicPositionChanged(this));
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
        if (dynamicPosHandlerNbt.containsUuid(NbtKeys.DYNAMIC_POS)) {
            dynamicPosEntityUuid = dynamicPosHandlerNbt.getUuid(NbtKeys.DYNAMIC_POS);
        }
        boolean afterRemovalHoldStatic = dynamicPosHandlerNbt.contains(NbtKeys.MOVE_TO_STATIC_POS_HANDLING)
                && dynamicPosHandlerNbt.getBoolean(NbtKeys.MOVE_TO_STATIC_POS_HANDLING);
        return new DynamicPosition(staticPos, dynamicPosEntityUuid, afterRemovalHoldStatic);
    }

    public void toNbt(NbtCompound nbt) {
        NbtCompound dynamicPosHandlerNbt = new NbtCompound();
        if (this.getStaticPos() != null) {
            NbtUtil.vec3dToNbt(dynamicPosHandlerNbt, NbtKeys.STATIC_POS, this.getStaticPos());
        }
        if (this.getDynamicPosUuid() != null) {
            dynamicPosHandlerNbt.putUuid(NbtKeys.DYNAMIC_POS, this.getDynamicPosUuid());
        }
        dynamicPosHandlerNbt.putBoolean(NbtKeys.MOVE_TO_STATIC_POS_HANDLING, this.afterRemovalHoldStatic);
        nbt.put(NbtKeys.DYNAMIC_POS_HANDLER, dynamicPosHandlerNbt);
    }

    public void registerListener(StateChangedCallback listener) {
        this.listeners.add(listener);
    }

    private void stateTest() {
        if (staticPos == null && dynamicPosUuid == null) {
            throw new IllegalStateException("Dynamic Position didn't conform to requirements");
        }
    }

    public void clearEntityCache() {
        this.dynamicPosEntityCache = null;
    }

    public interface StateChangedCallback {
        void onDynamicPositionChanged(DynamicPosition position);
    }
}
