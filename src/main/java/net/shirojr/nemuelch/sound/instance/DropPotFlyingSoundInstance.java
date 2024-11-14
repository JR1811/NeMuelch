package net.shirojr.nemuelch.sound.instance;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import org.jetbrains.annotations.NotNull;

public class DropPotFlyingSoundInstance extends MovingSoundInstance {
    private static final int DISTANCE_MAX = 80;
    private static final float VOLUME_MAX = 5.0f;

    private final DropPotEntity entity;

    private double previousIntensity = -1;
    private boolean isApexOfIntensity = true;

    public DropPotFlyingSoundInstance(@NotNull DropPotEntity entity) {
        super(NeMuelchSounds.POT_FLYING, SoundCategory.NEUTRAL);
        this.entity = entity;
        this.repeat = true;
        this.volume = VOLUME_MAX;
        updatePos();
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || this.entity.isRemoved()) {
            this.setDone();
            return;
        }
        float normalizedIntensity = (float) MathHelper.clamp(Math.min(1, ((DISTANCE_MAX - client.player.getPos().distanceTo(entity.getPos())) / DISTANCE_MAX)), 0, 1);

        this.pitch = Math.min(this.pitch, MathHelper.lerp(normalizedIntensity, 1.25f, 0.7f));
        if (this.previousIntensity > normalizedIntensity) {
            if (this.isApexOfIntensity) {
                this.isApexOfIntensity = false;
            }
            this.volume = MathHelper.lerp(normalizedIntensity, 0.0f, VOLUME_MAX);
        }

        this.previousIntensity = normalizedIntensity;
        updatePos();
    }

    private void updatePos() {
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }
}
