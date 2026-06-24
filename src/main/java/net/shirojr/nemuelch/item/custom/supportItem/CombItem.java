package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.CombEntityComponent;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class CombItem extends Item {
    private static final int RANGE = 3;

    public CombItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 7200;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public static boolean canCombTarget(LivingEntity target) {
        return target.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!isInUse(stack)) {
            LivingEntity currentTarget = getRaycastedTarget(user).orElse(user);
            if (canCombTarget(currentTarget)) {
                CombEntityComponent component = CombEntityComponent.get(currentTarget);
                component.startSession();
                setStoredTarget(stack, currentTarget);
                setInUse(stack, true);
                user.setCurrentHand(hand);
                return TypedActionResult.consume(stack);
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
        if (world instanceof ServerWorld serverWorld) {
            Entity currentTarget = getRaycastedTarget(user).orElse(user);
            Optional<LivingEntity> storedTarget = getStoredTarget(stack).map(serverWorld::getEntity)
                    .map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null);

            if (storedTarget.isPresent() && !storedTarget.get().equals(currentTarget)) {
                CombEntityComponent component = CombEntityComponent.get(storedTarget.get());
                component.stopSession();
                setStoredTarget(stack, null);
                user.stopUsingItem();
                storedTarget.get().damage(storedTarget.get().getDamageSources().cactus(), 2f);
                if (user instanceof PlayerEntity player) {
                    player.getItemCooldownManager().set(this, 40);
                }
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (world instanceof ServerWorld serverWorld) {
            resetEntityComb(serverWorld, stack);
        }
        setInUse(stack, false);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world instanceof ServerWorld serverWorld) {
            resetEntityComb(serverWorld, stack);
        }
        setInUse(stack, false);
        return super.finishUsing(stack, world, user);
    }

    public static Optional<LivingEntity> getRaycastedTarget(LivingEntity user) {
        Vec3d start = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(RANGE));

        Box searchBox = user.getBoundingBox().stretch(look.multiply(RANGE)).expand(1);
        EntityHitResult raycast = ProjectileUtil.raycast(user, start, end, searchBox,
                entity -> entity instanceof LivingEntity && !entity.equals(user), RANGE * RANGE);
        return Optional.ofNullable(raycast).map(EntityHitResult::getEntity).map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null);
    }

    public static Optional<UUID> getStoredTarget(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NbtKeys.TARGET_UUID)) return Optional.empty();
        return Optional.ofNullable(nbt.getUuid(NbtKeys.TARGET_UUID));
    }

    public static void setStoredTarget(ItemStack stack, @Nullable Entity target) {
        if (target == null) {
            if (stack.getNbt() != null) {
                stack.getNbt().remove(NbtKeys.TARGET_UUID);
            }
            return;
        }
        stack.getOrCreateNbt().putUuid(NbtKeys.TARGET_UUID, target.getUuid());
    }

    public static boolean isInUse(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NbtKeys.IN_USE)) return false;
        return nbt.getBoolean(NbtKeys.IN_USE);
    }

    public static void setInUse(ItemStack stack, boolean inUse) {
        stack.getOrCreateNbt().putBoolean(NbtKeys.IN_USE, inUse);
    }

    public static void resetEntityComb(ServerWorld world, ItemStack stack) {
        getStoredTarget(stack).ifPresent(uuid -> resetEntityComb(world, uuid));
        setStoredTarget(stack, null);
        setInUse(stack, false);
    }

    public static void resetEntityComb(ServerWorld world, UUID targetUuid) {
        Entity entity = world.getEntity(targetUuid);
        if (!(entity instanceof LivingEntity target)) return;
        CombEntityComponent component = CombEntityComponent.get(target);
        component.stopSession();
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        NbtCompound oldNbt = oldStack.copy().getOrCreateNbt();
        NbtCompound newNbt = newStack.copy().getOrCreateNbt();

        oldNbt.remove(NbtKeys.IN_USE);
        newNbt.remove(NbtKeys.IN_USE);

        oldNbt.remove(NbtKeys.TARGET_UUID);
        newNbt.remove(NbtKeys.TARGET_UUID);
        return oldNbt.equals(newNbt);
    }
}
