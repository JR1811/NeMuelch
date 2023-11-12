package net.shirojr.nemuelch.screen.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;

public class ParticleEmitterBlockScreenHandler extends ScreenHandler {
    protected ParticleEmitterBlockScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.isCreative() || player.isSpectator();
    }
}
