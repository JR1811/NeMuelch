package net.shirojr.nemuelch.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

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
    public double ominousHeartBeatRange = 10.0d;
    @Comment("Sets default values for the Gladius blade cane")
    public int gladiusBladeAttackDamage = 2;
    public float gladiusBladeAttackSpeed = -3f;
    public float trainingGloveAttackSpeed = -3.00f;
    public int trainingGloveMaxHits = 10;
    public int portableBarrelMaxFill = 20;
    public int smiteEnchantmentLevelCap = 5;
    public int baneEnchantmentLevelCap = 5;
    public int sharpnessEnchantmentLevelCap = 5;
    public int powerEnchantmentLevelCap = 5;
    public int knockbackEnchantmentLevelCap = 5;
    public int protectionEnchantmentLevelCap = 4;
    public int fireProtectionEnchantmentLevelCap = 4;
    public int fallProtectionEnchantmentLevelCap = 4;
    public int explosionProtectionEnchantmentLevelCap = 4;
    public int projectileProtectionEnchantmentLevelCap = 4;
    public int unbreakingEnchantmentLevelCap = 3;
    public boolean blockPlayerInventoryWhenFlying = true;
    public boolean badWeatherFlyingBlock = true;
    public double badWeatherDownForce = 0.05;
    public int badWeatherSafeBlockHeight = 3;
    public boolean ignitePlayersWithLavaBucket = true;
    public boolean startRenderingArrowsFunctionality = true;
    public float startRenderingArrowsAtHealth = 6.0f;
    public boolean stoneCutterDamage = true;
    public boolean specialPlayerLoot = true;
    public boolean frozenGroundPreventsCropBlockGrowth = true;
    public int pullToolDamage = 20;
    public int pullToolCooldown = 80;
    public double pullBodyHorizontal = 0.1;
    public double pullBodyVertical = 0.2;
    public int wateringCanFillRate = 20;
    public int wateringCanFillChance = 3;
    public int wateringCanCopperCapacity = 4;
    public int wateringCanIronCapacity = 6;
    public int wateringCanGoldCapacity = 12;
    public int wateringCanDiamondCapacity = 25;
    public boolean allowKnocking = true;
    public int knockableBlockRange = 3;
    public float knockingVolume = 2.0f;
}