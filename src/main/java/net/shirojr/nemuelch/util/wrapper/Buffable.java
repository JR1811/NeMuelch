package net.shirojr.nemuelch.util.wrapper;

import net.shirojr.nemuelch.util.AppliedLivingEntityAttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * LivingEntity data to handle temporary stat boosts
 */
public interface Buffable {
    @Nullable
    Integer nemuelch$getActiveStatTicksLeft(UUID modifierUuid);

    void nemuelch$setActiveStatTicksLeft(UUID modifierUuid, int ticks);

    @SuppressWarnings("UnusedReturnValue")
    boolean nemuelch$applyNewAttributeModifier(AppliedLivingEntityAttributeModifier modifier, int durationTicks);
}
