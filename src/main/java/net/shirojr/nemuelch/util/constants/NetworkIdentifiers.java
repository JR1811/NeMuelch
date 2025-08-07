package net.shirojr.nemuelch.util.constants;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NetworkIdentifiers {
    // C2S
    public static final Identifier KNOCKING_RAYCASTED_SOUND_C2S = NeMuelch.getId("knocking_raycasted");
    public static final Identifier SLEEP_EVENT_C2S = NeMuelch.getId("sleep_event");
    public static final Identifier MOUSE_SCROLLED_C2S = NeMuelch.getId("mouse_scrolled");

    // S2C
    public static final Identifier WATERING_CAN_PARTICLE_S2C = NeMuelch.getId("watering_can_fill");
    public static final Identifier SLEEP_EVENT_S2C = NeMuelch.getId("sleep_event");
    public static final Identifier CANCEL_SLEEP_EVENT_S2C = NeMuelch.getId("cancel_sleep_event");
    public static final Identifier START_SOUND_INSTANCE_S2C = NeMuelch.getId("start_sound_instance");
    public static final Identifier SOUND_PACKET_S2C = NeMuelch.getId("sound_packet");
    public static final Identifier PLAY_PARTICLE_S2C = NeMuelch.getId("particle_packet");
    public static final Identifier ENTITY_SPAWN_PACKET = NeMuelch.getId("spawn_packet");
    public static final Identifier POT_LAUNCHER_ACTIVATED = NeMuelch.getId("pot_launcher_activated");
}
