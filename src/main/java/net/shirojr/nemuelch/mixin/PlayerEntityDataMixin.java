package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDataMixin implements IBodyPartSaver {
    private NbtCompound persistentData;

    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }

        return persistentData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        if (persistentData != null) {
            nbt.put("nbt.nemuelch.missing_bodypart", persistentData);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("nbt.nemuelch.missing_bodypart")) {
            persistentData = nbt.getCompound("nbt.nemuelch.missing_bodypart");
        }
    }
}
