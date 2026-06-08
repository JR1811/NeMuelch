package net.shirojr.nemuelch.network;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.camera.CameraShakeHandler;
import net.shirojr.nemuelch.event.handler.ClientCountdownHandler;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class NeMuelchCache {
    public static final ClientCountdownHandler CLIENT_COUNTDOWN_HANDLER = new ClientCountdownHandler();
    public static final HashMap<Identifier, SoundInstance> SOUND_INSTANCE_CACHE = new HashMap<>();
    public static final CameraShakeHandler CAMERA_SHAKE_HANDLER = new CameraShakeHandler();
    public static int boatDeepWaterEnduranceTicks;
    public static double pullUpVertStrength;
    public static int maxAcidTicks;

    public static int getMaxAcidTicks(@Nullable World world) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getGameRules().getInt(NemuelchGameRules.ACIDIC_ATMOSPHERE_MAX_TICKS);
        } else {
            return maxAcidTicks;
        }
    }
}
