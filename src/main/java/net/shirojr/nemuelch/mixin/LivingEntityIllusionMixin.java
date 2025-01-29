package net.shirojr.nemuelch.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchTrackedData;
import net.shirojr.nemuelch.util.wrapper.Illusionable;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityIllusionMixin extends Entity implements Illusionable {
    @Unique
    private List<UUID> illusionTargetsPersistence = new ArrayList<>();

    @Unique
    private static final TrackedData<List<Integer>> ILLUSION_TARGETS_RUNTIME = DataTracker.registerData(LivingEntityIllusionMixin.class, NeMuelchTrackedData.ENTITY_LIST);
    @Unique
    private static final TrackedData<Boolean> IS_ILLUSION = DataTracker.registerData(LivingEntityIllusionMixin.class, TrackedDataHandlerRegistry.BOOLEAN);

    public LivingEntityIllusionMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initCustomDataTracker(CallbackInfo ci) {
        dataTracker.startTracking(ILLUSION_TARGETS_RUNTIME, new ArrayList<>());
        dataTracker.startTracking(IS_ILLUSION, false);
    }

    @Override
    public List<UUID> nemuelch$getPersistentIllusionTargets() {
        return Collections.unmodifiableList(this.illusionTargetsPersistence);
    }

    @Override
    public List<Entity> nemuelch$getIllusionTargets() {
        return NeMuelchTrackedData.resolveEntityIds(this.world, dataTracker.get(ILLUSION_TARGETS_RUNTIME));
    }

    @Override
    public void nemuelch$modifyIllusionTargets(Consumer<List<UUID>> newList, boolean sendClientUpdate) {
        newList.accept(this.illusionTargetsPersistence);
        if (sendClientUpdate && this.world instanceof ServerWorld serverWorld) {
            this.nemuelch$updateTrackedEntityIds(serverWorld);
        }
    }

    @Override
    public void nemuelch$clearIllusionTargets() {
        this.illusionTargetsPersistence.clear();
        if (this.world instanceof ServerWorld serverWorld) {
            this.nemuelch$updateTrackedEntityIds(serverWorld);
        }
    }

    @Override
    public boolean nemuelch$isIllusion() {
        return dataTracker.get(IS_ILLUSION);
    }

    @Override
    public void nemuelch$setIllusion(boolean isIllusion) {
        dataTracker.set(IS_ILLUSION, isIllusion);
        this.nemuelch$updateClients();
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataNbt(NbtCompound nbt, CallbackInfo ci) {
        this.nemuelch$modifyIllusionTargets(uuids -> {
            uuids.clear();
            NbtList nbtList = nbt.getList("IllusionTargets", NbtElement.STRING_TYPE);
            for (int i = 0; i < nbtList.size(); i++) {
                uuids.add(UUID.fromString(nbtList.getString(i)));
            }
        }, false);
        this.nemuelch$setIllusion(nbt.getBoolean("IsIllusion"));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList nbtList = new NbtList();
        for (UUID entry : nemuelch$getPersistentIllusionTargets()) {
            nbtList.add(NbtString.of(entry.toString()));
        }
        nbt.put("IllusionTargets", nbtList);
        nbt.putBoolean("IsIllusion", nemuelch$isIllusion());
    }

    @Override
    public void nemuelch$updateClients() {
        if (this.getWorld().isClient()) return;
        PlayerLookup.tracking(this).forEach(player -> {
            boolean isTarget = this.nemuelch$getIllusionTargets().contains(player);
            boolean isValidIllusion = this.nemuelch$isIllusion();

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(isTarget);
            buf.writeBoolean(isValidIllusion);
            buf.writeVarInt(this.getId());
            ServerPlayNetworking.send(player, NetworkIdentifiers.UPDATE_ILLUSIONS_CACHE_S2C, buf);
        });
    }

    @Override
    public void nemuelch$updateTrackedEntityIds(ServerWorld world) {
        List<Integer> entityIds = new ArrayList<>();
        for (UUID uuid : this.illusionTargetsPersistence) {
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                entityIds.add(entity.getId());
            }
        }
        this.dataTracker.set(ILLUSION_TARGETS_RUNTIME, entityIds);
        this.nemuelch$updateClients();
    }
}
