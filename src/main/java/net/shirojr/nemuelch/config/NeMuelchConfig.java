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
    public double respawnLocationX = -4;
    public double respawnLocationY = -48;
    public double respawnLocationZ = 8;

}
