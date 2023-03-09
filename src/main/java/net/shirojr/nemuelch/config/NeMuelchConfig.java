package net.shirojr.nemuelch.config;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "nemuelch")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class NeMuelchConfig implements ConfigData {

    @Comment("Set custom respawn location if a bed respawn has been set")
    public boolean useCustomBedRespawnLocation = false;

    @Comment("Set the coordinates of the custom respawn location")
    public double respawnLocationX = 0;
    public double respawnLocationY = 100;
    public double respawnLocationZ = 0;

    @Comment("Allows the beacon beam to go trough solid blocks")
    public boolean beamIgnoresSolidBlocks = false;

    @Comment("If Chicken Jockeys take up too much performance, you can disable their spawn with that option")
    public boolean blockJockeySpawn = false;

    @Comment("Enables fertilizable nether wart plant")
    public boolean fertilizableNetherWarts = false;

    public boolean campfireUtilities = true;

    public int arkadusCaneMaxCharge = 20;

    @Comment("Sets the values for the onion entity")
    public boolean onionEntityEnvironmentalDamage = true;
    public double onionEntityMaxHealth = 7.0D;
    public double onionEntityMovSpeed = 0.3D;
    public double onionEntityFollowRange = 20.0D;
    public int onionEntityExplosionRadius = 4;
    public float onionEntityEffectRadius = 10.0f;
    public int onionWandDurability = 7;
    public int onionEntitySummonableAmountMin = 1;
    public int onionEntitySummonableAmountMax = 7;

    @Comment("Configure bell settings")
    public float bellVolume = 2.0F;
    public float bellPitch = 1.0F;

    @Comment("Sets default values for the Gladius blade cane")
    public int gladiusBladeAttackDamage = 2;
    public float gladiusBladeAttackSpeed = -3f;

    public float trainingGloveAttackSpeed = -3.00f;
    public int trainingGloveMaxHits = 10;

    //public float ominousHeartVolume = 1f;
    //public float ominousHeartPitch = 1f;

    public int portableBarrelMaxFill = 20;

    public int protectionEnchantmentLevelCap = 4;
    public int smiteEnchantmentLevelCap = 5;

    public boolean blockPlayerInventoryWhenFlying = true;
    public boolean ignitePlayersWithLavaBucket = true;

    public boolean startRenderingArrowsFunctionality = true;
    public float startRenderingArrowsAtHealth = 6.0f;

    public boolean stoneCutterDamage = true;
}