package net.shirojr.nemuelch.client;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.camera.CameraShakeHandler;
import net.shirojr.nemuelch.event.custom.ClientCountdownHandler;

import java.util.HashMap;

public class NeMuelchClientCache {
    public static final ClientCountdownHandler CLIENT_COUNTDOWN_HANDLER = new ClientCountdownHandler();
    public static final HashMap<Identifier, SoundInstance> SOUND_INSTANCE_CACHE = new HashMap<>();
    public static final CameraShakeHandler CAMERA_SHAKE_HANDLER = new CameraShakeHandler();

    public static int boatDeepWaterEnduranceTicks;
    public static double pullUpVertStrength;
}
