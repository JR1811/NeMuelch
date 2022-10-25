package net.shirojr.nemuelch.config;

import blue.endless.jankson.Comment;
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
    public boolean blockJockeySpawnOnInitialize = false;

    @Comment("Enables fertilizable nether wart plant")
    public boolean fertilizableNetherWarts = false;

    @Comment("Sets the values for the onion entity")
    public boolean onionEntityEnvironmentalDamage = true;
    public double onionEntityMaxHealth = 7.0D;
    public double onionEntityMovSpeed = 0.3D;
    public double onionEntityFollowRange = 20.0D;
    public int onionEntityExplosionRadius = 4;
    public float onionEntityEffectRadius = 10.0f;
    public int onionEntitySummonableAmountMin = 1;
    public int onionEntitySummonableAmountMax = 7;

}
