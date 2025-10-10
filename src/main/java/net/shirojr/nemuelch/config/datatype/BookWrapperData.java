package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings("FieldMayBeFinal")
public class BookWrapperData {
    private int maxTooltipLineNumber;
    private int maxItemStorageAmount;
    private boolean showInsertionTime;
    public BookWrapperData(int maxTooltipLineNumber, int maxItemStorageAmount, boolean showInsertionTime) {
        this.maxTooltipLineNumber = maxTooltipLineNumber;
        this.maxItemStorageAmount = maxItemStorageAmount;
        this.showInsertionTime = showInsertionTime;
    }

    public int getMaxTooltipLineNumber() {
        return maxTooltipLineNumber;
    }

    public int getMaxItemStorageAmount() {
        return maxItemStorageAmount;
    }

    public boolean showsInsertionTime() {
        return showInsertionTime;
    }
}
