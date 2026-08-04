package net.shirojr.nemuelch.item.custom.supportItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.network.util.NetworkUtil;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;

import java.util.HashSet;
import java.util.UUID;

public class TalismanItem extends Item {
    public static final int COOLDOWN_BETWEEN_CHARGES = 10;
    private final int maxCharges;

    public TalismanItem(Settings settings, int maxCharges) {
        super(settings);
        this.maxCharges = maxCharges;
    }

    public int getMaxCharges() {
        return maxCharges;
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);


        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;
        ItemStack offHandStack = livingEntity.getOffHandStack();
        if (stack != offHandStack) {
            boolean notInSlot = false;
            if (entity instanceof PlayerEntity) notInSlot = true;
            else if (!selected) notInSlot = true;

            if (notInSlot) {
                NbtCompound nbt = stack.getNbt();
                if (nbt == null || !nbt.contains(NeMuelchNbtKeys.PROJECTILES)) return;
                nbt.remove(NeMuelchNbtKeys.PROJECTILES);
                return;
            }
        }
        for (ProjectileEntity projectile : serverWorld.getEntitiesByClass(ProjectileEntity.class, entity.getBoundingBox().expand(5), projectile -> true)) {
            if (addTargetedProjectileIfMissing(stack, projectile)) {
                useDefensiveCharge(livingEntity, projectile, stack);
            }
        }
    }

    public static int getCharges(ItemStack stack) {
        if (!(stack.getItem() instanceof TalismanItem)) return -1;
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.CHARGES)) return 0;
        return nbt.getInt(NeMuelchNbtKeys.CHARGES);
    }

    public void setCharges(ItemStack stack, int charges) {
        if (charges <= 0) {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;
            nbt.remove(NeMuelchNbtKeys.CHARGES);
            return;
        }
        stack.getOrCreateNbt().putInt(NeMuelchNbtKeys.CHARGES, MathHelper.clamp(charges, 0, this.getMaxCharges()));
    }

    public static HashSet<UUID> getTargetedProjectiles(ItemStack stack) {
        HashSet<UUID> result = new HashSet<>();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.PROJECTILES)) return result;
        for (NbtElement nbtElement : nbt.getList(NeMuelchNbtKeys.PROJECTILES, NbtElement.STRING_TYPE)) {
            result.add(UUID.fromString(nbtElement.asString()));
        }
        return result;
    }

    public static void setTargetProjectiles(ItemStack stack, HashSet<ProjectileEntity> projectiles) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList nbtList = new NbtList();
        for (ProjectileEntity projectile : projectiles) {
            nbtList.add(NbtString.of(projectile.getUuidAsString()));
        }
        nbt.put(NeMuelchNbtKeys.PROJECTILES, nbtList);
    }

    public boolean addTargetedProjectileIfMissing(ItemStack stack, ProjectileEntity projectile) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList nbtList = nbt.contains(NeMuelchNbtKeys.PROJECTILES) ? nbt.getList(NeMuelchNbtKeys.PROJECTILES, NbtElement.STRING_TYPE) : new NbtList();
        String newProjectileUuid = projectile.getUuidAsString();
        for (NbtElement nbtElement : nbtList) {
            String existingProjectileUuid = nbtElement.asString();
            if (newProjectileUuid.equals(existingProjectileUuid)) {
                return false;
            }
        }
        nbtList.add(NbtString.of(newProjectileUuid));
        nbt.put(NeMuelchNbtKeys.PROJECTILES, nbtList);
        return true;
    }

    public void useDefensiveCharge(LivingEntity user, ProjectileEntity projectile, ItemStack stack) {
        if (!(user.getWorld() instanceof ServerWorld)) return;
        if (user instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(this, COOLDOWN_BETWEEN_CHARGES);
        }
        // projectile.discard();
        //TODO: sound

        for (ServerPlayerEntity serverPlayerEntity : PlayerLookupUtil.trackingAndSelf(projectile)) {
            PacketByteBuf buf = PacketByteBufs.create();
            NetworkUtil.writeVec3d(buf, user.getPos().add(0, user.getHeight() / 2, 0));
            buf.writeVarInt(projectile.getId());
            buf.writeItemStack(stack);
            ServerPlayNetworking.send(serverPlayerEntity, NetworkIdentifiers.TALISMAN_DISCARD_PROJECTILE, buf);
        }
    }
}
