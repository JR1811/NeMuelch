package net.shirojr.nemuelch.datagen.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.sound.Sound;
import net.minecraft.util.math.random.Random;

public class SoundJsonSerializer {
    private static final Random RANDOM = Random.create();

    public JsonElement serialize(Sound sound) {
        if (this.isDefault(sound)) {
            return new JsonPrimitive(sound.getIdentifier().toString());
        }

        JsonObject json = new JsonObject();
        json.addProperty("name", sound.getIdentifier().toString());

        if (sound.getRegistrationType() == Sound.RegistrationType.SOUND_EVENT) {
            json.addProperty("type", "event");
        }

        float volume = sound.getVolume().get(RANDOM);
        if (volume != 1.0F) {
            json.addProperty("volume", volume);
        }

        float pitch = sound.getPitch().get(RANDOM);
        if (pitch != 1.0F) {
            json.addProperty("pitch", pitch);
        }

        if (sound.getWeight() != 1) {
            json.addProperty("weight", sound.getWeight());
        }

        if (sound.isStreamed()) {
            json.addProperty("stream", true);
        }

        if (sound.isPreloaded()) {
            json.addProperty("preload", true);
        }

        if (sound.getAttenuation() != 16) {
            json.addProperty("attenuation_distance", sound.getAttenuation());
        }

        return json;
    }

    private boolean isDefault(Sound sound) {
        return sound.getRegistrationType() == Sound.RegistrationType.FILE
                && sound.getVolume().get(RANDOM) == 1.0F
                && sound.getPitch().get(RANDOM) == 1.0F
                && sound.getWeight() == 1
                && !sound.isStreamed()
                && !sound.isPreloaded()
                && sound.getAttenuation() == 16;
    }
}
