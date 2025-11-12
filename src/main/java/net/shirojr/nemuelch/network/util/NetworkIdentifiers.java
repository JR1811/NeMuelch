package net.shirojr.nemuelch.network.util;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public interface NetworkIdentifiers {
    // C2S
    Identifier KNOCKING_RAYCASTED_SOUND_C2S = NeMuelch.getId("knocking_raycasted");
    Identifier MOUSE_SCROLLED_C2S = NeMuelch.getId("mouse_scrolled");
    Identifier MONSTER_ABILITY_KEY = NeMuelch.getId("monster_ability");

    // S2C
    Identifier WATERING_CAN_PARTICLE_S2C = NeMuelch.getId("watering_can_fill");
    Identifier CANCEL_SLEEP_EVENT_S2C = NeMuelch.getId("cancel_sleep_event");
    Identifier START_SOUND_INSTANCE_S2C = NeMuelch.getId("start_sound_instance");
    Identifier SOUND_PACKET_S2C = NeMuelch.getId("sound_packet");
    Identifier PLAY_PARTICLE_S2C = NeMuelch.getId("particle_packet");
    Identifier ENTITY_SPAWN_PACKET = NeMuelch.getId("spawn_packet");
    Identifier POT_LAUNCHER_ACTIVATED = NeMuelch.getId("pot_launcher_activated");
    Identifier TALISMAN_DISCARD_PROJECTILE = NeMuelch.getId("talisman_discard_projectile");
    Identifier SPAWN_ROTTEN_PARTICLE = NeMuelch.getId("spawn_rotten_particle");
}
