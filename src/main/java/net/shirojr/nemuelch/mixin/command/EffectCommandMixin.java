package net.shirojr.nemuelch.mixin.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.EffectCommand;
import net.shirojr.nemuelch.effect.util.UnremovableStatusEffectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EffectCommand.class)
public abstract class EffectCommandMixin {
    @WrapOperation(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
    private static boolean forceClearStatusEffects(LivingEntity instance, Operation<Boolean> original) {
        if (!(instance instanceof UnremovableStatusEffectHolder holder)) return original.call(instance);
        return holder.neMuelch$forceStatusEffectsClear();
    }

    @WrapOperation(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/registry/entry/RegistryEntry;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;removeStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    private static boolean forceRemoveStatusEffects(LivingEntity instance, StatusEffect type, Operation<Boolean> original) {
        if (!(instance instanceof UnremovableStatusEffectHolder holder)) return original.call(instance, type);
        return holder.neMuelch$forceStatusEffectRemoval(type);
    }
}
