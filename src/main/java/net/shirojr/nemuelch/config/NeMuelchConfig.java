package net.shirojr.nemuelch.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.config.datatype.*;

@Config(name = NeMuelch.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class NeMuelchConfig implements ConfigData {
    @Comment(
            "Toggle custom respawn locations with the \"respawnLocations\" Game Rule. " +
                    "Also enable the \"respawnLocationsDefaultFromConfig\" Game Rule to add default entry automatically to joining players."
    )
    public Vec3d defaultRespawnLocation = new Vec3d(0, 100, 0);
    @Comment("Allows the beacon beam to go through solid blocks")
    public boolean beamIgnoresSolidBlocks = false;
    @Comment("If Chicken Jockeys take up too much performance, you can disable their spawn with that option")
    public boolean blockJockeySpawn = false;
    @Comment("Enables fertilizable nether wart plant")
    public boolean fertilizableNetherWarts = false;
    public boolean campfireUtilities = true;
    public int arkadusCaneMaxCharge = 20;
    @Comment("Configure bell settings")
    public SoundData bellSound = new SoundData(2, 1);
    public double ominousHeartBeatRange = 10.0d;
    @Comment("Sets default values for the Gladius blade cane")
    public int gladiusBladeAttackDamage = 2;
    public float gladiusBladeAttackSpeed = -3f;
    public float trainingGloveAttackSpeed = -3.00f;
    public int trainingGloveMaxHits = 10;
    public int portableBarrelMaxFill = 20;
    @ConfigEntry.Gui.CollapsibleObject
    public EnchantmentLevelData enchantmentLevelCap = new EnchantmentLevelData(5, 5, 5, 5,
            5, 3, 4, 4, 4, 4, 4);
    public boolean ignitePlayersWithLavaBucket = true;
    public boolean startRenderingArrowsFunctionality = true;
    public float startRenderingArrowsAtHealth = 6.0f;
    public boolean stoneCutterDamage = true;
    public boolean specialPlayerLoot = true;
    public boolean frozenGroundPreventsCropBlockGrowth = true;
    @ConfigEntry.Gui.CollapsibleObject
    public PullBodyFeatureData pullBodyFeature = new PullBodyFeatureData(20, 80, 0.1, 0.2);
    @ConfigEntry.Gui.CollapsibleObject
    public WateringCanData wateringCan = new WateringCanData(20, 3,
            new WateringCanData.Material(4), new WateringCanData.Material(6),
            new WateringCanData.Material(12), new WateringCanData.Material(25));
    public boolean allowKnocking = true;
    public int knockableBlockRange = 5;
    public float knockingVolume = 2.0f;
    @Comment("For Whitelist Entries, check nemuelch Block Tags")
    public boolean enableFertilizableBlockWhitelistFeature = true;
    public boolean enableRandomTickChanceLimitFeature = true;
    public double actCommandMaxRange = 20;
    public boolean printActCommandInChat = true;
    public boolean printActCommandInActionBar = true;
    @ConfigEntry.Gui.CollapsibleObject
    public BookWrapperData bookWrapperItemData = new BookWrapperData(10, 2, true);
    public boolean disableReducedDebugInfoForOperators = true;
    @Comment("Enable this feature if multiple hearts rendering is not working. Coordinates start at bottom center of the screen")
    @ConfigEntry.Gui.CollapsibleObject
    public MiscGuiData guiBehaviour = new MiscGuiData(true, 20, Integer.MAX_VALUE, 100, 5);
    @Comment("If shovels interact with replaceable blocks, their path making is forwarded to the block below abd the " +
            "replaceable will be broken. Also check out the\"nemuelch:ignored_by_shovel_flattening\" block tag to specify additional replaceable blocks")
    public boolean forwardPathMakingThroughReplacables = true;
    public boolean restoreIrisShaderRenderingOnFinishedInternalShader = true;
    @ConfigEntry.Gui.CollapsibleObject
    public DummyData dummyEntityData = new DummyData(300, 60);
    @ConfigEntry.Gui.CollapsibleObject
    public FleetingNotesConfigData fleetingNotes = new FleetingNotesConfigData(false, 0.075f, "0xCCFFFFFF");
    public float speedLimiterIncrement = 0.05f;
}