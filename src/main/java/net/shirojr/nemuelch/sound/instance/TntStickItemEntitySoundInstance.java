package net.shirojr.nemuelch.sound.instance;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;
import net.shirojr.nemuelch.sound.NeMuelchSounds;

public class TntStickItemEntitySoundInstance extends MovingSoundInstance {
    private final TntStickItemEntity tntStickItemEntity;
    private float distance = 0.0f;

    protected TntStickItemEntitySoundInstance(TntStickItemEntity entity) {
        super(NeMuelchSounds.TNT_STICK_BURN, SoundCategory.NEUTRAL);
        this.tntStickItemEntity = entity;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
        this.setPosition(this.tntStickItemEntity.getX(), this.tntStickItemEntity.getY(), this.tntStickItemEntity.getZ());
    }

    @Override
    public boolean canPlay() {
        return !tntStickItemEntity.isSilent();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if (this.tntStickItemEntity.isRemoved()) {
            this.setDone();
            return;
        }
        setPosition(this.tntStickItemEntity.getX(), this.tntStickItemEntity.getY(), this.tntStickItemEntity.getZ());
        float horizontalLength = (float)this.tntStickItemEntity.getVelocity().horizontalLength();
        if (horizontalLength >= 0.01f) {
            this.distance = MathHelper.clamp(this.distance + 0.0025f, 0.0f, 1.0f);
            this.volume = MathHelper.lerp(MathHelper.clamp(horizontalLength, 0.0f, 0.5f), 0.0f, 0.7f);
        } else {
            this.distance = 0.0f;
            this.volume = 0.0f;
        }
    }

    private void setPosition(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }
}
