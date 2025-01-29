package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.util.AppliedLivingEntityAttributeModifier;
import net.shirojr.nemuelch.util.NbtKeys;
import net.shirojr.nemuelch.util.wrapper.Buffable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityBuffMixin implements Buffable {
    @Unique
    HashMap<AppliedLivingEntityAttributeModifier, Integer> activeModifierDuration = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickTempBuffs(CallbackInfo ci) {
        for (var entry : this.activeModifierDuration.entrySet()) {
            if (entry.getValue() <= 0) {
                onFinished(entry.getKey());
                continue;
            }
            this.activeModifierDuration.put(entry.getKey(), entry.getValue() - 1);
        }
    }

    @Nullable
    @Override
    public Integer nemuelch$getActiveStatTicksLeft(UUID modifierUuid) {
        for (var entry : this.activeModifierDuration.entrySet()) {
            if (!entry.getKey().getId().equals(modifierUuid)) continue;
            return entry.getValue();
        }
        return null;
    }

    @Override
    public void nemuelch$setActiveStatTicksLeft(UUID modifierUuid, int ticks) {
        for (var entry : this.activeModifierDuration.entrySet()) {
            if (!entry.getKey().getId().equals(modifierUuid)) continue;
            this.activeModifierDuration.put(entry.getKey(), ticks);
            return;
        }
    }

    @Override
    public boolean nemuelch$applyNewAttributeModifier(AppliedLivingEntityAttributeModifier appliedModifier, int durationTicks) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityAttributeInstance instance = entity.getAttributeInstance(appliedModifier.attribute());
        if (instance == null) return false;
        instance.addTemporaryModifier(appliedModifier.modifier());
        nemuelch$setActiveStatTicksLeft(appliedModifier.getId(), durationTicks);
        return true;
    }

    @Unique
    private void onFinished(AppliedLivingEntityAttributeModifier activeModifier) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient()) return;
        EntityAttributeInstance instance = entity.getAttributeInstance(activeModifier.attribute());
        if (instance != null) {
            instance.removeModifier(activeModifier.getId());
            this.activeModifierDuration.remove(activeModifier);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readTempBuffFromNbt(NbtCompound nbt, CallbackInfo ci) {
        for (String entryKey : nbt.getCompound(NbtKeys.APPLIED_ENTITY_ATTRIBUTE_MODIFIER).getKeys()) {
            NbtCompound entryNbt = nbt.getCompound(entryKey);
            AppliedLivingEntityAttributeModifier appliedModifier = AppliedLivingEntityAttributeModifier.fromNbt(entryNbt);
            if (appliedModifier == null) continue;
            int duration = nbt.getInt("duration");
            this.nemuelch$applyNewAttributeModifier(appliedModifier, duration);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeTempBuffToNbt(NbtCompound nbt, CallbackInfo ci) {

    }
}
