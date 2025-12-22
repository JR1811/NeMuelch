package net.shirojr.nemuelch.util.duck;

public interface BoatDespawnHandler {
    String TIME_NBT_KEY = "StartEmptiedTime";

    long neMuelch$getBoatEmptiedTime();

    void neMuelch$setBoatEmptiedTime(long time);

    default void stopCountDown() {
        neMuelch$setBoatEmptiedTime(-1);
    }

    default boolean isCountDownActive() {
        return neMuelch$getBoatEmptiedTime() >= 0;
    }
}
