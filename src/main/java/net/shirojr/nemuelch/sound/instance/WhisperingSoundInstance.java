package net.shirojr.nemuelch.sound.instance;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.ArtifactItem;

public class WhisperingSoundInstance extends MovingSoundInstance {
    private static final int END_PHASE_DURATION = 80;
    public static final float MAX_VOLUME = 0.7f, LOW_VOLUME = 0.5f;

    private final PlayerEntity player;
    private int endTick;
    private boolean finish;

    public WhisperingSoundInstance(PlayerEntity player) {
        super(NeMuelchSounds.WHISPERS, SoundCategory.PLAYERS);
        this.player = player;
        this.endTick = END_PHASE_DURATION;
        this.finish = false;

        this.pitch = 1.0f;
        this.volume = MAX_VOLUME;
        this.repeat = true;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (player == null || player.isDead() || this.endTick <= 0) {
            this.setDone();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        if (!(player.getOffHandStack().getItem() instanceof ArtifactItem) && !(player.getMainHandStack().getItem() instanceof ArtifactItem)) {
            this.finish = true;
        }

        if (player.isSneaking() && this.volume != LOW_VOLUME) {
            this.volume = LOW_VOLUME;
        } else if (!player.isSneaking() && this.volume != MAX_VOLUME) {
            this.volume = MAX_VOLUME;
        }

        if (this.finish) {
            this.volume = MathHelper.lerp((float) this.endTick / END_PHASE_DURATION, 0.0f, player.isSneaking() ? LOW_VOLUME : MAX_VOLUME);
            this.endTick--;
        }
    }

    public void shouldFinish(boolean finish) {
        this.finish = finish;
    }
}
