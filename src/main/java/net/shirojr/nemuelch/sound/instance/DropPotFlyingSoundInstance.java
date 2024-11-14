package net.shirojr.nemuelch.sound.instance;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import org.jetbrains.annotations.NotNull;

public class DropPotFlyingSoundInstance extends MovingSoundInstance {
    private static final int MAX_DISTANCE = 50 /*DropPotEntity.RENDER_DISTANCE / 2*/;
    private static final float MAX_VOLUME = 3, MAX_PITCH = 1.1f, MIN_PITCH = 0.7f;

    private final DropPotEntity entity;
    private float previousVolume = -1;

    public DropPotFlyingSoundInstance(@NotNull DropPotEntity entity) {
        super(NeMuelchSounds.POT_FLYING, SoundCategory.NEUTRAL);
        this.entity = entity;
        this.repeat = true;
        this.volume = MAX_VOLUME;

        updatePos();
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || this.entity.isRemoved()) {
            this.setDone();
            return;
        }
        float normalizedDistance = (float) MathHelper.clamp(Math.min(1, ((MAX_DISTANCE - client.player.getPos().distanceTo(entity.getPos())) / MAX_DISTANCE)), 0, 1);
        this.pitch = Math.min(this.pitch, MathHelper.lerp(normalizedDistance, MAX_PITCH, MIN_PITCH));

        float tmpVolume = MathHelper.lerp(normalizedDistance, 0.0f, MAX_VOLUME);
        if (this.previousVolume > tmpVolume) {
            this.volume = tmpVolume;
        }
        this.previousVolume = tmpVolume;

        updatePos();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    private void updatePos() {
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }
}
