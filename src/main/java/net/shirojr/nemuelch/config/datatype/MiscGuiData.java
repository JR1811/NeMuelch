package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings("FieldMayBeFinal")
public class MiscGuiData {
    private boolean enableHpAmountTextRendering;
    private int minHpTextRenderingAmount;
    private int hpTextRenderingPosY;
    private int hpTextRenderingPosX;

    public MiscGuiData(boolean enableHpAmountTextRendering, int minHpTextRenderingAmount, int hpTextRenderingPosX, int hpTextRenderingPosY) {
        this.enableHpAmountTextRendering = enableHpAmountTextRendering;
        this.minHpTextRenderingAmount = minHpTextRenderingAmount;
        this.hpTextRenderingPosX = hpTextRenderingPosX;
        this.hpTextRenderingPosY = hpTextRenderingPosY;
    }

    public boolean enabledHpAmountTextRendering() {
        return enableHpAmountTextRendering;
    }

    public int getMinHpTextRenderingAmount() {
        return minHpTextRenderingAmount;
    }

    public int getHpTextRenderingPosX() {
        return hpTextRenderingPosX;
    }

    public int getHpTextRenderingPosY() {
        return hpTextRenderingPosY;
    }
}
