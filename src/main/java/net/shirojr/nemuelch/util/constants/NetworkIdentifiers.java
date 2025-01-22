package net.shirojr.nemuelch.util.constants;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NetworkIdentifiers {
    // C2S
    public static final Identifier KNOCKING_RANGED_SOUND_C2S = new Identifier(NeMuelch.MOD_ID, "knocking_ranged");
    public static final Identifier KNOCKING_RAYCASTED_SOUND_C2S = new Identifier(NeMuelch.MOD_ID, "knocking_raycasted");
    public static final Identifier SLEEP_EVENT_C2S = new Identifier(NeMuelch.MOD_ID, "sleep_event");
    public static final Identifier PARTICLE_EMITTER_UPDATE_C2S = new Identifier(NeMuelch.MOD_ID, "particle_emitter_update");
    public static final Identifier MOUSE_SCROLLED_C2S = new Identifier(NeMuelch.MOD_ID, "mouse_scrolled");

    // S2C
    public static final Identifier WATERING_CAN_PARTICLE_S2C = new Identifier(NeMuelch.MOD_ID, "watering_can_fill");
    public static final Identifier SLEEP_EVENT_S2C = new Identifier(NeMuelch.MOD_ID, "sleep_event");
    public static final Identifier CANCEL_SLEEP_EVENT_S2C = new Identifier(NeMuelch.MOD_ID, "cancel_sleep_event");
    public static final Identifier START_SOUND_INSTANCE_S2C = new Identifier(NeMuelch.MOD_ID, "start_sound_instance");
    public static final Identifier INIT_OBFUSCATED_CACHE_S2C = new Identifier(NeMuelch.MOD_ID, "init_obfuscated_cache");
    public static final Identifier UPDATE_OBFUSCATED_CACHE_S2C = new Identifier(NeMuelch.MOD_ID, "update_obfuscated_cache");
    public static final Identifier UPDATE_ILLUSIONS_CACHE_S2C = new Identifier(NeMuelch.MOD_ID, "update_illusions_cache");
    public static final Identifier SOUND_PACKET_S2C = new Identifier(NeMuelch.MOD_ID, "sound_packet");
    public static final Identifier PLAY_PARTICLE_S2C = new Identifier(NeMuelch.MOD_ID, "particle_packet");
    public static final Identifier ENTITY_SPAWN_PACKET = new Identifier(NeMuelch.MOD_ID, "spawn_packet");
    public static final Identifier LEASH_TRACKING_UPDATE_S2C = new Identifier(NeMuelch.MOD_ID, "leash_tracking_update");
}
