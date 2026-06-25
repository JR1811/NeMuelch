package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.shirojr.nemuelch.NeMuelch;

import java.util.List;

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
    SoundEvent HIT_DEITY = register("hit_deity");

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

    SoundEvent SPLASHES_RUMBLE_LOW = register("splashes_rumble_low");
    SoundEvent SQUIRT = register("squirt");
    SoundEvent EATING_CRUNCHY = register("eating_crunchy");
    SoundEvent EATING_DIGESTION = register("eating_digestion");

    SoundEvent MAGIC_CHARGE_UP = register("magic_charge_up");
    SoundEvent MAGIC_CHARGE_UP_CRUSHED = register("magic_charge_up_crushed");
    SoundEvent MAGIC_CHARGE_DOWN = register("magic_charge_down");
    SoundEvent MAGIC_CHARGE_DOWN_CRUSHED = register("magic_charge_down_crushed");

    SoundEvent ANCIENT_CREATURE_CALL_1 = register("ancient_creature_call_1");

    SoundEvent PULL_UP = register("pull_up");

    SoundEvent CRYPTIC_POEM_01 = register("cryptic_poem_dead_captain");
    SoundEvent CRYPTIC_CHANT_01 = register("cryptic_chant_01");
    SoundEvent CRYPTIC_CHANT_02 = register("cryptic_chant_02");

    SoundEvent CHILD_GIGGLE = register("child_giggle");
    SoundEvent CHILD_LAUGH_1 = register("child_laugh_1");
    SoundEvent CHILD_LAUGH_2 = register("child_laugh_2");

    SoundEvent MONSTER_COW = register("monster_cow");
    SoundEvent MONSTER_COW_REVERSED = register("monster_cow_reversed");
    SoundEvent MONSTER_DEER_01 = register("monster_deer_01");
    SoundEvent MONSTER_DEER_02 = register("monster_deer_02");
    SoundEvent MONSTER_DEER_DEEP_01 = register("monster_deer_deep_01");
    SoundEvent MONSTER_DEER_DEEP_02 = register("monster_deer_deep_02");
    SoundEvent MONSTER_BABY_GOAT = register("monster_baby_goat");
    List<SoundEvent> MONSTERS = List.of(MONSTER_COW, MONSTER_COW_REVERSED, MONSTER_DEER_01, MONSTER_DEER_02, MONSTER_DEER_DEEP_01,
            MONSTER_DEER_DEEP_02, MONSTER_BABY_GOAT);

    SoundEvent HORN_FOGHORN_01 = register("horn_foghorn_01");
    SoundEvent HORN_FOGHORN_02 = register("horn_foghorn_02");
    SoundEvent HORN_FOGHORN_03 = register("horn_foghorn_03");
    SoundEvent HORN_FOGHORN_04 = register("horn_foghorn_04");
    SoundEvent HORN_FOGHORN_05 = register("horn_foghorn_05");
    List<SoundEvent> HORN_FOGHORNS = List.of(HORN_FOGHORN_01, HORN_FOGHORN_02, HORN_FOGHORN_03, HORN_FOGHORN_04, HORN_FOGHORN_05);

    SoundEvent COUGH = register("cough");
    SoundEvent KINDLE = register("kindle");

    SoundEvent HUH = register("huh");

    SoundEvent ENTITY_ACID_BURN = register("entity_acid_burn");

    SoundEvent RICOCHET = register("ricochet");
    SoundEvent IMPACT_HEAVY = register("impact_heavy");

    SoundEvent COMB = register("comb");

    SoundEvent SWOOSH = register("swoosh");

    static SoundEvent register(String id) {
        SoundEvent sound = SoundEvent.of(NeMuelch.getId(id));
        return Registry.register(Registries.SOUND_EVENT, NeMuelch.getId(id), sound);
    }

    static void initialize() {
        // static initialisation
    }
}
