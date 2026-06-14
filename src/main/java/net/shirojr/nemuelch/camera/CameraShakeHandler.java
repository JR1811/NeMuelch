package net.shirojr.nemuelch.camera;

import net.minecraft.entity.Entity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"UnusedReturnValue"})
public class CameraShakeHandler {

    @Nullable
    private DisplacementSequence activeDisplacementSequence;
    private Entity focusedEntity;

    public CameraShakeHandler() {
        this.focusedEntity = null;
        this.activeDisplacementSequence = null;
    }

    public @Nullable DisplacementSequence getActiveDisplacementSequence() {
        return activeDisplacementSequence;
    }

    public void setActiveDisplacementSequence(@Nullable DisplacementSequence activeDisplacementSequence) {
        if (activeDisplacementSequence != null && NeMuelchConfigInit.CONFIG.disableCameraUtil) {
            return;
        }
        this.activeDisplacementSequence = activeDisplacementSequence;
    }

    public Entity getFocusedEntity() {
        return focusedEntity;
    }

    public CameraShakeHandler setFocusedEntity(Entity focusedEntity) {
        this.focusedEntity = focusedEntity;
        return this;
    }

    public void tick() {
        if (getActiveDisplacementSequence() == null) return;
        if (!getActiveDisplacementSequence().isActive()) return;
        getActiveDisplacementSequence().tick();
    }

    public void stopDisplacement() {
        if (this.activeDisplacementSequence == null) return;
        this.activeDisplacementSequence.clear();
    }
}
