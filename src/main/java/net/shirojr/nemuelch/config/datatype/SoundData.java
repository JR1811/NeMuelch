package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class SoundData {
    private float volume, pitch;

    public SoundData(float volume, float pitch) {
        this.volume = volume;
        this.pitch = pitch;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
