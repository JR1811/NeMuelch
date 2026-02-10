package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.util.duck.BoatDespawnHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity implements VariantHolder<BoatEntity.Type>, BoatDespawnHandler {
    @Unique
    private long startEmptyTime = -1;

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    private BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public long neMuelch$getBoatEmptiedTime() {
        return startEmptyTime;
    }

    @Override
    public void neMuelch$setBoatEmptiedTime(long time) {
        startEmptyTime = time;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void additionalTickLogic(CallbackInfo ci) {
        handleBoatDespawning(getWorld());
    }

    @Unique
    private void handleBoatDespawning(World world) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (this.getType().isIn(NeMuelchTags.EntityTypes.DESPAWN_PROTECTED)) return;
        if (!isCountDownActive()) return;
        int despawnDuration = serverWorld.getGameRules().getInt(NemuelchGameRules.EMPTY_BOAT_DESPAWN_DURATION);
        if (despawnDuration == -1) return;
        long currentTime = serverWorld.getTime();
        if (currentTime >= neMuelch$getBoatEmptiedTime() + despawnDuration) {
            onCountDownFinished();
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void getDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(TIME_NBT_KEY)) {
            neMuelch$setBoatEmptiedTime(nbt.getLong(TIME_NBT_KEY));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void applyDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putLong(TIME_NBT_KEY, neMuelch$getBoatEmptiedTime());
    }

    @Unique
    private void onCountDownFinished() {
        damage(this.getDamageSources().outOfWorld(), 40);
    }
}
