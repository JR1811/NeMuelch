package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings("FieldMayBeFinal")
public class DummyData {
    private int displayDuration;
    private int baseAnimationDuration;

    public DummyData(int displayDuration, int baseAnimationDuration) {
        this.displayDuration = displayDuration;
        this.baseAnimationDuration = baseAnimationDuration;
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public int getBaseAnimationDuration() {
        return baseAnimationDuration;
    }
}
