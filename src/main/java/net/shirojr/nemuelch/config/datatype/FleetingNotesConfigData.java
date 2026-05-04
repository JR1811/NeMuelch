package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class FleetingNotesConfigData {
    private boolean preventGeneralFleetingNotesRendering;


    public FleetingNotesConfigData(boolean preventGeneralFleetingNotesRendering) {
        this.preventGeneralFleetingNotesRendering = preventGeneralFleetingNotesRendering;
    }

    public boolean preventGeneralFleetingNotesRendering() {
        return preventGeneralFleetingNotesRendering;
    }
}
