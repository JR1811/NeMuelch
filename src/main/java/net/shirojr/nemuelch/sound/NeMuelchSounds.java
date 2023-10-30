package net.shirojr.nemuelch.sound;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;


public class NeMuelchSounds {

    public static SoundEvent SCREAM_ANGUISH = of("scream_anguish");

    public static SoundEvent EXPLOSION_CRUMBLING = of("explosion_crumbling");

    public static SoundEvent DRONE_CREATURE = of("drone_creature");
    public static SoundEvent DRONE_FACTORY = of("drone_factory");
    public static SoundEvent DRONE_STATIC = of("drone_static");

    public static SoundEvent ENVIRONMENT_MOUNTAIN = of("environment_mountain");
    public static SoundEvent ENVIRONMENT_UNDERWATER = of("environment_underwater");

    public static SoundEvent ENTITY_ONION_HISS = of("entity_onion_hiss");
    public static SoundEvent ENTITY_ONION_FLAP = of("entity_onion_flap");
    public static SoundEvent ENTITY_ONION_SQUEEL_DEATH = of("entity_onion_squeel_death");
    public static SoundEvent ENTITY_ONION_SQUEEL_ONE = of("entity_onion_squeel_1");
    public static SoundEvent ENTITY_ONION_SQUEEL_TWO = of("entity_onion_squeel_2");
    public static SoundEvent ENTITY_ONION_SQUEEL_THREE = of("entity_onion_squeel_3");
    public static SoundEvent ENTITY_ONION_SQUEEL_HURT = of("entity_onion_squeel_hurt");
    public static SoundEvent ENTITY_ONION_SWARM = of("entity_onion_swarm");

    public static SoundEvent ITEM_OMINOUS_HEART = of("item_ominous_heart");
    public static SoundEvent ITEM_RUNE = of("item_energy_release");
    public static SoundEvent ITEM_RADIATOR_ACTIVATION = of("item_radiator_activation");
    public static SoundEvent TNT_STICK_DROP = of("item_tnt_stick_drop");
    public static SoundEvent TNT_STICK_BURN = of("item_tnt_stick_burn");

    public static SoundEvent KNOCKING_01 = of("knocking_01");
    public static SoundEvent EVENT_SLEEP_AMBIENT = of("event_sleep_ambient");

    static SoundEvent of(String id) {
        SoundEvent sound = new SoundEvent(new Identifier(NeMuelch.MOD_ID, id));
        return Registry.register(Registry.SOUND_EVENT, new Identifier(NeMuelch.MOD_ID, id), sound);
    }

    public static void initializeSounds() {
        // empty method loads in jvm and initializes NeMuelchSounds class
        NeMuelch.LOGGER.info("Registering " + NeMuelch.MOD_ID + " Sounds");
    }
}
