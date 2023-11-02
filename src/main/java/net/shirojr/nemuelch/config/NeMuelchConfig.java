package net.shirojr.nemuelch.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.config.datatype.*;

@Config(name = "nemuelch")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class NeMuelchConfig implements ConfigData {
    @Comment("Set custom respawn location if a bed respawn has been set")
    public boolean useCustomBedRespawnLocation = false;
    @Comment("Set the coordinates of the custom respawn location")
    public Vec3d respawnLocation = new Vec3d(0, 100, 0);
    @Comment("Allows the beacon beam to go trough solid blocks")
    public boolean beamIgnoresSolidBlocks = false;
    @Comment("If Chicken Jockeys take up too much performance, you can disable their spawn with that option")
    public boolean blockJockeySpawn = false;
    @Comment("Enables fertilizable nether wart plant")
    public boolean fertilizableNetherWarts = false;
    public boolean campfireUtilities = true;
    public int arkadusCaneMaxCharge = 20;
    @Comment("Sets the values for the onion entity")
    public OnionData onion = new OnionData(true, 7.0, 0.3,
            20, 4, 10, 7, 1, 7);
    @Comment("Configure bell settings")
    public SoundData bellSound = new SoundData(2, 1);
    public double ominousHeartBeatRange = 10.0d;
    @Comment("Sets default values for the Gladius blade cane")
    public int gladiusBladeAttackDamage = 2;
    public float gladiusBladeAttackSpeed = -3f;
    public float trainingGloveAttackSpeed = -3.00f;
    public int trainingGloveMaxHits = 10;
    public int portableBarrelMaxFill = 20;
    public EnchantmentLevelData enchantmentLevelCap = new EnchantmentLevelData(5, 5, 5, 5,
            5, 3, 4, 4, 4, 4, 4);
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
    public PullBodyFeatureData pullBodyFeature = new PullBodyFeatureData(20, 80, 0.1, 0.2);
    public WateringCanData wateringCan = new WateringCanData(20, 3,
            new WateringCanData.Material(4), new WateringCanData.Material(6),
            new WateringCanData.Material(12), new WateringCanData.Material(25));
    public boolean allowKnocking = true;
    public int knockableBlockRange = 3;
    public float knockingVolume = 2.0f;
    public int specialSleepEventChance = 5;
}