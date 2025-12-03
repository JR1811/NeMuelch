package net.shirojr.nemuelch.sound.instance;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.shirojr.nemuelch.sound.SoundData;
import org.jetbrains.annotations.NotNull;

public class FollowingRepeatableSoundInstance extends MovingSoundInstance {
    private final int maxRepeats;

    @NotNull
    private Entity source;
    private int repeatCounter;

    public FollowingRepeatableSoundInstance(@NotNull Entity source, SoundData soundData) {
        super(soundData.sound(), soundData.category(), SoundInstance.createRandom());
        this.source = source;
        this.maxRepeats = soundData.repeat();
        this.repeatCounter = 0;
        this.volume = soundData.volume();
        this.pitch = soundData.pitch();
        this.repeat = soundData.repeat() > 0;
        this.repeatDelay = 1;   // min 1 otherwise more difficult to increment repeats...
    }

    public @NotNull Entity getSource() {
        return source;
    }

    public void setSource(@NotNull Entity source) {
        this.source = source;
    }

    public int getMaxRepeats() {
        return maxRepeats;
    }

    public int getRepeatCounter() {
        return repeatCounter;
    }

    public void setRepeatCounter(int repeatCounter) {
        this.repeatCounter = repeatCounter;
    }

    public void incrementRepeatCounter() {
        this.setRepeatCounter(this.getRepeatCounter() + 1);
    }

    @Override
    public boolean canPlay() {
        return getRepeatCounter() < getMaxRepeats();
    }

    @Override
    public void tick() {
        this.x = source.getX();
        this.y = source.getY();
        this.z = source.getZ();

        if (getRepeatCounter() >= getMaxRepeats() || source.isRemoved()) {
            this.setDone();
        }
    }
}
