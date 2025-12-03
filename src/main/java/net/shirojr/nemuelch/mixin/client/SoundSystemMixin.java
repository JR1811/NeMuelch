package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.shirojr.nemuelch.sound.instance.FollowingRepeatableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundSystem;isRepeatDelayed(Lnet/minecraft/client/sound/SoundInstance;)Z"))
    private void incrementRepeatCounter(CallbackInfo ci, @Local SoundInstance soundInstance) {
        if (!(soundInstance instanceof FollowingRepeatableSoundInstance followingSoundInstance)) return;
        followingSoundInstance.incrementRepeatCounter();
    }
}
