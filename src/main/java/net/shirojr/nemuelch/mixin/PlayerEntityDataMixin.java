package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.wrapper.ICustomPlayerEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDataMixin extends LivingEntity implements ICustomPlayerEntityData {
    @Unique
    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final TrackedData<NbtCompound> TRACKED_NBT_DATA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    protected PlayerEntityDataMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(TRACKED_NBT_DATA, new NbtCompound());
    }

    @Override
    public NbtCompound nemuelch$getPersistentData() {
        return getDataTracker().get(TRACKED_NBT_DATA);
    }

    @Override
    public <T> T nemuelch$editPersistentData(Function<NbtCompound, T> action) {
        var wrapper = this.nemuelch$getPersistentData().copy();
        T result = action.apply(wrapper);
        this.dataTracker.set(TRACKED_NBT_DATA, wrapper);
        return result;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound customNbtData = this.dataTracker.get(TRACKED_NBT_DATA);

        if (!customNbtData.isEmpty()) {
            nbt.put(ICustomPlayerEntityData.CUSTOM_PLAYER_DATA_NBT_KEY, customNbtData);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(ICustomPlayerEntityData.CUSTOM_PLAYER_DATA_NBT_KEY)) {
            this.dataTracker.set(TRACKED_NBT_DATA, nbt.getCompound(ICustomPlayerEntityData.CUSTOM_PLAYER_DATA_NBT_KEY));
        }
    }
}
