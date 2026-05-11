package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.util.helper.NbtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FleetingNoteData {
    private int timeLeft;
    private boolean markedForRemoval;
    private float visibleDistance;
    private float angle;
    private final List<Text> lines;

    public FleetingNoteData(int timeLeft, float visibleDistance, float angle, List<Text> lines) {
        this.timeLeft = timeLeft;
        this.markedForRemoval = timeLeft == 0;
        this.visibleDistance = visibleDistance;
        this.angle = angle;
        this.lines = lines;
    }

    public void tick() {
        if (this.timeLeft <= 0) return;
        this.setTimeLeft(this.getTimeLeft() - 1);
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
        this.markedForRemoval = this.timeLeft == 0;
    }

    public float getVisibleDistance() {
        return visibleDistance;
    }

    public void setVisibleDistance(float visibleDistance) {
        this.visibleDistance = Math.max(0, visibleDistance);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public List<Text> getLines() {
        return lines;
    }

    public void markForRemoval(boolean markForRemoval) {
        this.markedForRemoval = markForRemoval;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void toNbt(NbtCompound nbt) {
        nbt.putInt("time", this.timeLeft);
        NbtList linesNbt = new NbtList();
        nbt.putFloat("visibleDistance", this.visibleDistance);
        nbt.putFloat("angle", this.angle);
        for (Text line : lines) {
            String json = Text.Serializer.toJson(line);
            linesNbt.add(NbtString.of(json));
        }
        nbt.put("lines", linesNbt);
    }

    public static FleetingNoteData fromNbt(NbtCompound nbt) {
        int time = nbt.getInt("time");
        float visibleDistance = nbt.getFloat("visibleDistance");
        float angle = nbt.getFloat("angle");
        List<Text> lines = new ArrayList<>();
        NbtList linesNbt = nbt.getList("lines", NbtElement.STRING_TYPE);
        for (int i = 0; i < linesNbt.size(); i++) {
            lines.add(Text.Serializer.fromJson(linesNbt.getString(i)));
        }
        return new FleetingNoteData(time, visibleDistance, angle, lines);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FleetingNoteData) obj;
        return this.timeLeft == that.timeLeft &&
                Objects.equals(this.lines, that.lines) &&
                this.visibleDistance == that.visibleDistance &&
                this.angle == that.angle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeLeft, lines, visibleDistance, angle);
    }

    @Override
    public String toString() {
        return "FleetingNoteData[pos=%s, visibleDistance=%s, angle=%s, lines=%s]".formatted(
                timeLeft, visibleDistance, angle, lines
        );
    }

    public record Positioned(Vec3d pos, FleetingNoteData data) {
        public void toNbt(NbtCompound nbt) {
            NbtUtil.vec3dToNbt(nbt, "pos", pos);
            data.toNbt(nbt);
        }

        public static Positioned fromNbt(NbtCompound nbt) {
            Vec3d pos = NbtUtil.vec3dFromNbt(nbt, "pos");
            FleetingNoteData data = FleetingNoteData.fromNbt(nbt);
            return new Positioned(pos, data);
        }
    }
}