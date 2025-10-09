package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings("FieldMayBeFinal")
public class BookWrapperData {
    private int maxTooltipLineNumber;

    public BookWrapperData(int maxTooltipLineNumber) {
        this.maxTooltipLineNumber = maxTooltipLineNumber;
    }

    public int getMaxTooltipLineNumber() {
        return maxTooltipLineNumber;
    }
}
