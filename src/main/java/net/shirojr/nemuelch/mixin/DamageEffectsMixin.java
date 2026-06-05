package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.sound.SoundEvent;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Debug(export = true)
@Mixin(DamageEffects.class)
public enum DamageEffectsMixin {
    NEMUELCH_ACID_BURNING("acid_burning", NeMuelchSounds.ENTITY_ACID_BURN);

    @Shadow
    DamageEffectsMixin(String id, SoundEvent sound) {
    }
}
