package net.shirojr.nemuelch.datagen.util;

import net.minecraft.client.sound.Sound;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;

public class SoundOptions {
    private final String name;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private int weight = 1;
    private Sound.RegistrationType type = Sound.RegistrationType.FILE;
    private boolean stream = false;
    private boolean preload = false;
    private int attenuation = 16;

    public SoundOptions(String name) { this.name = name; }

    public SoundOptions volume(float volume) { this.volume = volume; return this; }
    public SoundOptions pitch(float pitch) { this.pitch = pitch; return this; }
    public SoundOptions weight(int weight) { this.weight = weight; return this; }
    public SoundOptions event() { this.type = Sound.RegistrationType.SOUND_EVENT; return this; }
    public SoundOptions stream(boolean stream) { this.stream = stream; return this; }
    public SoundOptions preload(boolean preload) { this.preload = preload; return this; }
    public SoundOptions attenuationDistance(int distance) { this.attenuation = distance; return this; }

    public Sound build() {
        return new Sound(name, ConstantFloatProvider.create(volume), ConstantFloatProvider.create(pitch),
                weight, type, stream, preload, attenuation);
    }
}
