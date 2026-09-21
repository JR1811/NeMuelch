package net.shirojr.nemuelch.screen.util;

import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class LeftCenterTooltipPositioner implements TooltipPositioner {
    private final int margin;

    public LeftCenterTooltipPositioner(int margin) {
        this.margin = margin;
    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        return new Vector2i(this.margin, (screenHeight - height) / 2);
    }
    
    
}
