package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

@SuppressWarnings("unused")
public class NeMuelchSounds {
    public static SoundEvent SCREAM_ANGUISH = register("scream_anguish");

    public static SoundEvent EXPLOSION_CRUMBLING = register("explosion_crumbling");

    public static SoundEvent DRONE_CREATURE = register("drone_creature");
    public static SoundEvent DRONE_FACTORY = register("drone_factory");
    public static SoundEvent DRONE_STATIC = register("drone_static");

    public static SoundEvent ENVIRONMENT_MOUNTAIN = register("environment_mountain");
    public static SoundEvent ENVIRONMENT_UNDERWATER = register("environment_underwater");

    public static SoundEvent ITEM_OMINOUS_HEART = register("item_ominous_heart");
    public static SoundEvent ITEM_RUNE = register("item_energy_release");
    public static SoundEvent ITEM_RADIATOR_ACTIVATION = register("item_radiator_activation");
    public static SoundEvent TNT_STICK_DROP = register("item_tnt_stick_drop");
    public static SoundEvent TNT_STICK_BURN = register("item_tnt_stick_burn");

    public static SoundEvent KNOCKING_01 = register("knocking_01");
    public static SoundEvent EVENT_SLEEP_AMBIENT = register("event_sleep_ambient");

    public static SoundEvent WHISPERS = register("whispers");

    public static SoundEvent POT_RELEASE = register("pot_release");
    public static SoundEvent POT_FLYING = register("pot_flying");
    public static SoundEvent POT_HIT = register("pot_hit");
    public static final SoundEvent POT_LAND = register("pot_land");

    public static final SoundEvent LAUNCHER_TURN = register("launcher_turn");
    public static final SoundEvent LAUNCHER_LAUNCH = register("pot_launcher_launch");

    public static final SoundEvent SHEARS_SNAP = register("shears_snap");


    static SoundEvent register(String id) {
        SoundEvent sound = SoundEvent.of(NeMuelch.getId(id));
        return Registry.register(Registries.SOUND_EVENT, new Identifier(NeMuelch.MOD_ID, id), sound);
    }

    public static void initialize() {
        // static initialisation
    }
}
