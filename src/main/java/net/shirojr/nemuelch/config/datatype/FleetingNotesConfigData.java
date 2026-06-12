package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class FleetingNotesConfigData {
    private boolean preventGeneralFleetingNotesRendering;
    private float spriteSize;
    private String spriteColorArgb;


    public FleetingNotesConfigData(boolean preventGeneralFleetingNotesRendering, float spriteSize, String spriteColorArgb) {
        this.preventGeneralFleetingNotesRendering = preventGeneralFleetingNotesRendering;
        this.spriteSize = spriteSize;
        this.spriteColorArgb = spriteColorArgb;
    }

    public boolean preventGeneralFleetingNotesRendering() {
        return preventGeneralFleetingNotesRendering;
    }

    public float getSpriteSize() {
        return spriteSize;
    }

    public String getSpriteColorArgb() {
        return spriteColorArgb;
    }
}
