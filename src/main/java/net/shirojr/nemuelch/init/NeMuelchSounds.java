package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

@SuppressWarnings("unused")
public interface NeMuelchSounds {
    SoundEvent SCREAM_ANGUISH = register("scream_anguish");

    SoundEvent EXPLOSION_CRUMBLING = register("explosion_crumbling");

    SoundEvent DRONE_CREATURE = register("drone_creature");
    SoundEvent DRONE_FACTORY = register("drone_factory");
    SoundEvent DRONE_STATIC = register("drone_static");

    SoundEvent ENVIRONMENT_MOUNTAIN = register("environment_mountain");
    SoundEvent ENVIRONMENT_UNDERWATER = register("environment_underwater");

    SoundEvent ITEM_OMINOUS_HEART = register("item_ominous_heart");
    SoundEvent ITEM_RUNE = register("item_energy_release");
    SoundEvent ITEM_RADIATOR_ACTIVATION = register("item_radiator_activation");
    SoundEvent TNT_STICK_DROP = register("item_tnt_stick_drop");
    SoundEvent TNT_STICK_BURN = register("item_tnt_stick_burn");

    SoundEvent KNOCKING_01 = register("knocking_01");
    SoundEvent EVENT_SLEEP_AMBIENT = register("event_sleep_ambient");

    SoundEvent WHISPERS = register("whispers");

    SoundEvent POT_RELEASE = register("pot_release");
    SoundEvent POT_FLYING = register("pot_flying");
    SoundEvent POT_HIT = register("pot_hit");
    SoundEvent POT_LAND = register("pot_land");

    SoundEvent LAUNCHER_TURN = register("launcher_turn");
    SoundEvent LAUNCHER_LAUNCH = register("pot_launcher_launch");

    SoundEvent SHEARS_SNAP = register("shears_snap");

    SoundEvent WOLF_HOWL = register("wolf_howl");
    SoundEvent PLANT_SWING = register("plant_swing");
    SoundEvent HUMAN_GROWL = register("human_growl");
    SoundEvent BLOOD_SUCK = register("blood_suck");
    SoundEvent VAMPIRE_HURT = register("vampire_hurt");
    SoundEvent VAMPIRE_VOMIT = register("vampire_vomit");
    SoundEvent VAMPIRE_SCREAM = register("vampire_scream");


    static SoundEvent register(String id) {
        SoundEvent sound = SoundEvent.of(NeMuelch.getId(id));
        return Registry.register(Registries.SOUND_EVENT, new Identifier(NeMuelch.MOD_ID, id), sound);
    }

    static void initialize() {
        // static initialisation
    }
}
