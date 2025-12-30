package net.shirojr.nemuelch.occasion;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchCustomRegistries;
import net.shirojr.nemuelch.occasion.util.OccasionState;
import net.shirojr.nemuelch.occasion.util.OccasionType;

import java.util.Objects;

public class OccasionEntry {
    public static final String TYPE_NBT_KEY = "Type";
    public static final String START_TIME_NBT_KEY = "StartTime";
    public static final String DURATION_NBT_KEY = "Duration";

    private final OccasionType type;
    private long startTime;
    private long duration;

    public OccasionEntry(OccasionType type) {
        this(type, -1);
    }

    public OccasionEntry(OccasionType type, long startTime) {
        this(type, startTime, type.defaultDuration());
    }

    public OccasionEntry(OccasionType type, long startTime, long duration) {
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
    }

    public OccasionType getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public OccasionState getState(long currentTime) {
        if (startTime == -1 || duration == -1) {
            return OccasionState.DISABLED;
        }
        if (currentTime >= startTime && currentTime < startTime + getDuration()) {
            return OccasionState.ACTIVE;
        }
        if (currentTime >= startTime + getDuration()) {
            return OccasionState.FINISHED;
        }
        return OccasionState.INACTIVE;
    }

    public boolean isDisabled() {
        return startTime == -1 || getDuration() == -1;
    }

    public boolean intersects(OccasionEntry other) {
        if (this.isDisabled() || other.isDisabled()) return false;

        long startTimeSelf = this.getStartTime();
        long startTimeOther = other.getStartTime();
        if (startTimeSelf == startTimeOther) return true;
        long endTimeSelf = startTimeSelf + this.getDuration();
        long endTimeOther = startTimeOther + other.getDuration();

        return startTimeSelf < endTimeOther && startTimeOther < endTimeSelf;
    }

    /**
     * @return <code>-1</code> if occasion is not in progress
     */
    public double getNormalizedProgress(long time) {
        if (getState(time) != OccasionState.ACTIVE) return -1f;
        long elapsed = Math.max(0, time - getStartTime());
        return (double) elapsed / getDuration();
    }

    public void onStart(World world) {
        this.getType().onStart(world, this);
    }

    public void onActiveTick(World world) {
        this.getType().onActiveTick(world, this);
    }

    public void onFinish(World world) {
        this.getType().onFinish(world, this);
    }

    public void onPlayerLeftWorldWhileActive(ServerWorld world) {
        this.getType().onPlayerLeftWorldWhileActive(world, this);
    }

    public void tick(World world) {
        long time = world.getTime();
        OccasionState currentState = getState(time);
        if (currentState == OccasionState.FINISHED) {
            onFinish(world);
            return;
        }
        if (time == getStartTime()) {
            onStart(world);
            return;
        }
        if (currentState == OccasionState.ACTIVE) {
            onActiveTick(world);
        }
    }

    public void toNbt(NbtCompound nbt) {
        Identifier identifier = NeMuelchCustomRegistries.OCCASIONS.getId(this.getType());
        if (identifier == null) {
            throw new IllegalStateException("Occasion Type was not registered");
        }
        nbt.putString(TYPE_NBT_KEY, identifier.toString());
        nbt.putLong(START_TIME_NBT_KEY, getStartTime());
        nbt.putLong(DURATION_NBT_KEY, getDuration());
    }

    public static OccasionEntry fromNbt(NbtCompound nbt) {
        if (!nbt.contains(TYPE_NBT_KEY) || !nbt.contains(START_TIME_NBT_KEY) || !nbt.contains(DURATION_NBT_KEY)) {
            throw new IllegalStateException("Invalid Occasion NBT Format");
        }
        Identifier identifier = Identifier.tryParse(nbt.getString(TYPE_NBT_KEY));
        if (identifier == null) {
            throw new IllegalStateException("Invalid Occasion Type Identifier Format");
        }
        OccasionType occasionType = NeMuelchCustomRegistries.OCCASIONS.get(identifier);
        if (occasionType == null) {
            throw new IllegalStateException("Occasion Type was not registered");
        }
        long startTime = nbt.getLong(START_TIME_NBT_KEY);
        long duration = nbt.getLong(DURATION_NBT_KEY);
        return new OccasionEntry(occasionType, startTime, duration);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OccasionEntry other)) return false;
        return getStartTime() == other.getStartTime() && getDuration() == other.getDuration() && Objects.equals(getType(), other.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getStartTime(), getDuration());
    }
}
