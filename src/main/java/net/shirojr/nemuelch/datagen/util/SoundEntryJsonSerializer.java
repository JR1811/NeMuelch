package net.shirojr.nemuelch.datagen.util;

import com.google.gson.*;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEntry;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Check out {@link net.minecraft.client.sound.SoundEntryDeserializer SoundEntryDeserializer} for the counterpart
 */
public class SoundEntryJsonSerializer implements JsonSerializer<SoundEntry> {
    private final SoundJsonSerializer soundSerializer = new SoundJsonSerializer();

    @Override
    public JsonElement serialize(SoundEntry entry, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        if (entry.canReplace()) {
            json.addProperty("replace", true);
        }
        if (entry.getSubtitle() != null) {
            json.addProperty("subtitle", entry.getSubtitle());
        }
        json.add("sounds", this.serializeSounds(entry.getSounds()));
        return json;
    }

    private JsonArray serializeSounds(List<Sound> sounds) {
        JsonArray array = new JsonArray();
        for (Sound sound : sounds) {
            array.add(this.soundSerializer.serialize(sound));
        }
        return array;
    }
}
