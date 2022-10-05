package net.shirojr.nemuelch.util.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchSounds {

    public static SoundEvent ONION_WALK = of("");

    static SoundEvent of(String id) {
        SoundEvent sound = new SoundEvent(new Identifier(NeMuelch.MOD_ID, id));
        Registry.register(Registry.SOUND_EVENT, new Identifier(NeMuelch.MOD_ID, id), sound);
        return sound;
    }

}


