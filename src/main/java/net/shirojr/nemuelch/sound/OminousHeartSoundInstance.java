package net.shirojr.nemuelch.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.supportItem.OminousHeartItem;

public class OminousHeartSoundInstance extends MovingSoundInstance {
    private final PlayerEntity player;
    private int tickCount;

    public OminousHeartSoundInstance(PlayerEntity user) {
        super(NeMuelchSounds.ITEM_OMINOUS_HEART, SoundCategory.PLAYERS);
        this.player = user;
    }

    @Override
    public boolean canPlay() {
        return !this.player.isSilent() && !this.player.isDead();
    }

    @Override
    public void tick() {
        if (tickCount % 60 != 0) return;

        if (!this.player.isRemoved() && !this.player.isDead()) {
            this.x = (float)this.player.getX();
            this.y = (float)this.player.getY();
            this.z = (float)this.player.getZ();
            if (this.player.isSneaking()) {
                this.volume = 2.0F;
                this.pitch = 0.75F;
            } else {
                this.volume = 5.0F;
                this.pitch = 1.5F;
            }
        } else {
            tickCount = 0;
            this.setDone();
        }

        tickCount++;
    }
}
