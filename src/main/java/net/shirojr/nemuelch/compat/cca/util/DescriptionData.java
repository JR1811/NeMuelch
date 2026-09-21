package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DescriptionData {
    private final Text content;
    @Nullable
    private final List<UUID> allowedViewers;
    private int duration;
    private final double visibleDistance;
    private final float deviationAngle;

    public DescriptionData(Text content, @Nullable List<UUID> allowedViewers, int duration, double visibleDistance, float deviationAngle) {
        this.content = content;
        this.allowedViewers = allowedViewers;
        this.duration = duration;
        this.visibleDistance = visibleDistance;
        this.deviationAngle = deviationAngle;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        if (duration == -1) this.duration = -1;
        else this.duration = Math.max(0, duration);
    }

    public void decrementDuration() {
        if (this.duration <= 0) return;
        this.setDuration(this.getDuration() - 1);
    }

    public boolean isFinished() {
        return this.getDuration() != -1 && this.getDuration() <= 0;
    }

    public Text getContent() {
        return content;
    }

    public boolean canBeSeenBy(PlayerEntity viewer) {
        return this.allowedViewers == null || this.allowedViewers.contains(viewer.getUuid());
    }

    public double getVisibleDistance() {
        return visibleDistance;
    }

    public float getDeviationAngle() {
        return deviationAngle;
    }

    @Nullable
    public static DescriptionData fromNbt(NbtCompound nbt) {
        if (!nbt.contains(NeMuelchNbtKeys.CONTENT)) return null;
        if (!nbt.contains(NeMuelchNbtKeys.DURATION)) return null;
        if (!nbt.contains(NeMuelchNbtKeys.DISTANCE)) return null;
        if (!nbt.contains(NeMuelchNbtKeys.DEVIATION)) return null;

        Text content = Text.Serializer.fromJson(nbt.getString(NeMuelchNbtKeys.CONTENT));
        List<UUID> targetUuids = null;
        if (nbt.contains(NeMuelchNbtKeys.TARGETS)) {
            targetUuids = new ArrayList<>();
            NbtList targetsUuidNbt = nbt.getList(NeMuelchNbtKeys.TARGETS, NbtElement.INT_ARRAY_TYPE);
            for (NbtElement entry : targetsUuidNbt) {
                targetUuids.add(NbtHelper.toUuid(entry));
            }
        }
        int duration = nbt.getInt(NeMuelchNbtKeys.DURATION);
        double distance = nbt.getDouble(NeMuelchNbtKeys.DISTANCE);
        float deviation = nbt.getFloat(NeMuelchNbtKeys.DEVIATION);
        return new DescriptionData(content, targetUuids, duration, distance, deviation);
    }

    public void toNbt(NbtCompound nbt) {
        nbt.putString(NeMuelchNbtKeys.CONTENT, Text.Serializer.toJson(this.content));
        if (this.allowedViewers == null) {
            nbt.remove(NeMuelchNbtKeys.TARGETS);
        } else {
            NbtList targetsNbt = new NbtList();
            for (UUID targetUuid : this.allowedViewers) {
                targetsNbt.add(NbtHelper.fromUuid(targetUuid));
            }
            nbt.put(NeMuelchNbtKeys.TARGETS, targetsNbt);
        }
        nbt.putInt(NeMuelchNbtKeys.DURATION, this.duration);
        nbt.putDouble(NeMuelchNbtKeys.DISTANCE, this.visibleDistance);
        nbt.putFloat(NeMuelchNbtKeys.DEVIATION, this.deviationAngle);
    }
}
