package net.shirojr.nemuelch.util.duck;

import net.minecraft.entity.mob.MobEntity;

/**
 * Default persistency methods don't allow for full control
 * @see MobEntity#setPersistent()
 */
public interface MobPersistency {
    boolean nemuelch$isPersistent();

    void nemuelch$setPersistent(boolean persistent);
}
