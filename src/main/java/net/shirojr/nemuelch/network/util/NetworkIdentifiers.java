package net.shirojr.nemuelch.network.util;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public interface NetworkIdentifiers {
    // C2S
    Identifier KNOCKING_RAYCASTED_SOUND_C2S = NeMuelch.getId("knocking_raycasted");
    Identifier MOUSE_SCROLLED_POT_LAUNCHER_C2S = NeMuelch.getId("mouse_scrolled_pot_launcher");
    Identifier MOUSE_SCROLLED_SLOWING_C2S = NeMuelch.getId("mouse_scrolled_slowing");

    // S2C
    Identifier WATERING_CAN_PARTICLE_S2C = NeMuelch.getId("watering_can_fill");
    Identifier CANCEL_SLEEP_EVENT_S2C = NeMuelch.getId("cancel_sleep_event");
    Identifier START_SOUND_INSTANCE_S2C = NeMuelch.getId("start_sound_instance");
    Identifier SOUND_PACKET_S2C = NeMuelch.getId("sound");
    Identifier PLAY_PARTICLE_S2C = NeMuelch.getId("particle");
    Identifier ENTITY_SPAWN = NeMuelch.getId("spawn");
    Identifier POT_LAUNCHER_ACTIVATED = NeMuelch.getId("pot_launcher_activated");
    Identifier TALISMAN_DISCARD_PROJECTILE = NeMuelch.getId("talisman_discard_projectile");
    Identifier SPAWN_ROTTEN_PARTICLE = NeMuelch.getId("spawn_rotten_particle");
    Identifier THIRD_PERSON_ITEM_RENDERING = NeMuelch.getId("third_person_item_rendering");
    Identifier CAMERA_DISPLACEMENT_SEQUENCE_START = NeMuelch.getId("camera_displacement_sequence_start");
    Identifier CAMERA_DISPLACEMENT_SEQUENCE_START_SCALED = NeMuelch.getId("camera_displacement_sequence_start_scaled");
    Identifier CAMERA_DISPLACEMENT_SEQUENCE_STOP = NeMuelch.getId("camera_displacement_sequence_stop");
    Identifier CAMERA_DISPLACEMENT_SEQUENCE_STOP_ALL = NeMuelch.getId("camera_displacement_sequence_stop_all");
    Identifier START_FOLLOWING_SOUND_INSTANCE = NeMuelch.getId("start_following_sound_instance");
    Identifier STOP_FOLLOWING_SOUND_INSTANCE = NeMuelch.getId("stop_following_sound_instance");
    Identifier ADVANCED_FOG_SCREEN_DATA_CHANGE = NeMuelch.getId("advanced_fog_screen_data_change");
    Identifier ADVANCED_FOG_SYNC = NeMuelch.getId("advanced_fog_sync");
    Identifier ADVANCED_FOG_REQUEST_SELF_SYNC = NeMuelch.getId("advanced_fog_request_self_sync");
    Identifier DEEP_WATER_BOAT_ENDURANCE_SYNC = NeMuelch.getId("deep_water_endurance_sync");
    Identifier PULL_UP_VERT_STRENGTH_GAMERULE_SYNC = NeMuelch.getId("pull_up_vert_strength_gamerule_sync");
    Identifier STRING_TO_CLIENT_CLIPBOARD = NeMuelch.getId("to_client_clipboard");
    // Identifier FADE_TO_BLACK = NeMuelch.getId("fade_to_black");
    // Identifier FADE_FROM_BLACK = NeMuelch.getId("fade_from_black");
    // Identifier FADE_SHADER = NeMuelch.getId("fade_shader");
    Identifier SHADER_CLEAR = NeMuelch.getId("shader_clear");
    Identifier SHADER_INTENSITY_SETTER = NeMuelch.getId("shader_intensity_setter");
    Identifier SHADER_TRANSITION_START = NeMuelch.getId("shader_transition_start");
}
