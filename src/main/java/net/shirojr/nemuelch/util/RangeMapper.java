package net.shirojr.nemuelch.util;

public record RangeMapper(double oldMin, double oldMax, double newMin, double newMax) {
    public double getRemappedDoubleValue(double number) {
        return calculateReMappedValue(number, this.oldMin, this.oldMax, this.newMin, this.newMax);
    }

    public float getRemappedFloatValue(double number) {
        return (float) calculateReMappedValue(number, this.oldMin, this.oldMax, this.newMin, this.newMax);
    }

    public static double calculateReMappedValue(double number, double oldMin, double oldMax, double newMin, double newMax) {
        return ((number - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
    }
}
