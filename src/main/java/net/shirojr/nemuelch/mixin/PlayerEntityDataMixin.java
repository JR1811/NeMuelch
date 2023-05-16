package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDataMixin extends LivingEntity implements IBodyPartSaver {
    // private NbtCompound persistentData;

    @Shadow public abstract void remove(RemovalReason reason);

    @SuppressWarnings("WrongEntityDataParameterClass")
    private static final TrackedData<NbtCompound> HIDDEN_BODYPARTS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    protected PlayerEntityDataMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(HIDDEN_BODYPARTS, new NbtCompound());
    }

    @Override
    public NbtCompound getPersistentData() {

        return getDataTracker().get(HIDDEN_BODYPARTS);
    }

    /**
     *
     * @param action
     */
    @Override
    public <T> T editPersistentData(Function<NbtCompound, T> action) {
        var wrapper = this.getPersistentData().copy();

        T result = action.apply(wrapper);
        this.dataTracker.set(HIDDEN_BODYPARTS, wrapper);
        return result;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound hiddenParts = this.dataTracker.get(HIDDEN_BODYPARTS);

        if (!hiddenParts.isEmpty()) {
            nbt.put("nbt.nemuelch.missing_bodypart", hiddenParts);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    protected void nemuelch$injectCustomReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("nbt.nemuelch.missing_bodypart")) {
            this.dataTracker.set(HIDDEN_BODYPARTS, nbt.getCompound("nbt.nemuelch.missing_bodypart"));
        }
    }
}
