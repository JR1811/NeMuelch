package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEnchantments;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ClimbingPickItem extends PickaxeItem {
    protected final int maxUseTime;
    protected final double maxRange;
    protected final int earlyReleasePunishmentTime;

    public ClimbingPickItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings,
                            int maxUseTime, double maxRange, int earlyReleasePunishmentTime) {
        super(material, attackDamage, attackSpeed, settings);
        this.maxUseTime = maxUseTime;
        this.maxRange = maxRange;
        this.earlyReleasePunishmentTime = MathHelper.clamp(earlyReleasePunishmentTime, 0, maxUseTime);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NEMUELCH_CLIMBING;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.maxUseTime;
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getHookedDuration(stack) > 0 || stack.isDamaged();
    }

    public double getModifiedMaxRange(ItemStack stack) {
        int alpinistLevel = getAlpinistEnchantmentLevel(stack);
        if (alpinistLevel <= 0) return this.maxRange;
        return this.maxRange * 0.5;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int maxUseTime = this.getMaxUseTime(stack);
        int hookedDuration = getHookedDuration(stack);
        if (hookedDuration <= 0) {
            return super.getItemBarStep(stack);
        }
        float normalized = MathHelper.clamp(((float) hookedDuration) / maxUseTime, 0f, 1f);
        return Math.round(normalized * 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int hookedDuration = getHookedDuration(stack);
        if (hookedDuration <= 0) {
            return super.getItemBarColor(stack);
        }
        float hue = hookedDuration < this.earlyReleasePunishmentTime ? 0 : 0.3f;
        return MathHelper.hsvToRgb(hue, 1f, 1f);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        List<String> noAnimationNbtKeys = List.of(NeMuelchNbtKeys.USAGE_TICKS);
        NbtCompound oldNbt = oldStack.getNbt();
        if (oldNbt != null) oldNbt = oldNbt.copy();
        NbtCompound newNbt = newStack.getNbt();
        if (newNbt != null) newNbt = newNbt.copy();

        if (oldNbt != null) {
            noAnimationNbtKeys.forEach(oldNbt::remove);
        }
        if (newNbt != null) {
            noAnimationNbtKeys.forEach(newNbt::remove);
        }
        return !Objects.equals(oldNbt, newNbt);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        float original = super.getMiningSpeedMultiplier(stack, state);
        return original <= 1 ? original : original * 0.5f;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.hasVehicle()) {
            return super.use(world, user, hand);
        }

        ItemStack stack = user.getStackInHand(hand);

        BlockHitResult hitResult = raycast(world, user, this.getModifiedMaxRange(stack));
        Direction direction = hitResult.getSide();
        // if (direction.getAxis().isVertical()) return super.use(world, user, hand);
        if (hitResult.getType() != HitResult.Type.BLOCK) return super.use(world, user, hand);
        /*if (!world.getBlockState(hitResult.getBlockPos()).isIn(NeMuelchTags.Blocks.PICKAXE_CLIMBABLE)) {
            return super.use(world, user, hand);
        }*/

        user.setCurrentHand(hand);
        Vec3d hookPos = hitResult.getPos();
        NbtCompound hookPosNbt = new NbtCompound();
        Vec3dHelper.toNbt(hookPosNbt, hookPos);

        setHookPos(stack, hookPos);
        double distance = user.getEyePos().distanceTo(hookPos);
        setHookDistance(stack, distance);

        createParticles(world, hitResult);

        if (user instanceof ServerPlayerEntity player) {
            handleVelocityModification(player, entity -> entity.setVelocity(0, 0, 0), false);
            ServerWorld serverWorld = player.getServerWorld();
            serverWorld.playSound(
                    null, hookPos.x, hookPos.y, hookPos.z, NeMuelchSounds.METAL_STRIKE, SoundCategory.MASTER, 1f, 1f
            );
            BlockState state = world.getBlockState(hitResult.getBlockPos());
            serverWorld.playSound(
                    null, hookPos.x, hookPos.y, hookPos.z, state.getSoundGroup().getBreakSound(), SoundCategory.MASTER, 2f, 1f
            );
        }

        return super.use(world, user, hand);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
        Vec3d hookPos = getHookPos(stack);
        Optional<Double> optRadius = getHookDistance(stack);
        if (optRadius.isEmpty() || hookPos == null || remainingUseTicks <= 1) {
            user.stopUsingItem();
            return;
        }

        double radius = optRadius.get();
        Vec3d oldPos = user.getPos();
        Vec3d lookVec = user.getRotationVec(1.0F);
        Vec3d newEyePos = hookPos.subtract(lookVec.multiply(radius));
        Vec3d newPos = newEyePos.subtract(0, user.getStandingEyeHeight(), 0);
        Vec3d velocity = newPos.subtract(oldPos);
        double modifiedMaxRange = this.getModifiedMaxRange(stack);
        boolean outOfRange = newEyePos.squaredDistanceTo(hookPos) >= modifiedMaxRange * modifiedMaxRange;
        if (outOfRange) {
            user.stopUsingItem();
            return;
        }
        if (isHookObstructed(world, user, hookPos, newEyePos) && getAlpinistEnchantmentLevel(stack) <= 0) {
            user.stopUsingItem();
            return;
        }

        if (user instanceof ServerPlayerEntity player) {
            setHookedDuration(stack, this.getMaxUseTime(stack) - remainingUseTicks);
            if (getAlpinistEnchantmentLevel(stack) <= 0) {
                handleVelocityModification(player, entity -> entity.setVelocity(velocity), true);
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;

        BlockHitResult hitResult = raycast(world, user, this.getModifiedMaxRange(stack));

        if (nbt.contains(NeMuelchNbtKeys.POS) && world instanceof ServerWorld serverWorld) {
            Vec3d hookPos = getHookPos(stack);
            if (hookPos != null) {
                serverWorld.playSound(null, hookPos.x, hookPos.y, hookPos.z, NeMuelchSounds.METAL_RELEASE, SoundCategory.MASTER, 2f, 1f);
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                serverWorld.playSound(
                        null, hookPos.x, hookPos.y, hookPos.z, state.getSoundGroup().getBreakSound(), SoundCategory.MASTER, 2f, 1f
                );
            }
        }
        setHookPos(stack, null);
        setHookDistance(stack, -1);
        setHookedDuration(stack, -1);
        stack.damage(2, user, e -> e.sendToolBreakStatus(e.getActiveHand()));

        createParticles(world, hitResult);

        if (world instanceof ServerWorld) {
            double scaledDamping = MathHelper.lerp(getNormalizedSoaringEnchantmentLevel(stack), 0.3, 1);
            handleVelocityModification(user, entity -> entity.setVelocity(user.getVelocity().multiply(scaledDamping)), false);
        }

        int alpinistLevel = getAlpinistEnchantmentLevel(stack);
        if (alpinistLevel > 0 && user instanceof ServerPlayerEntity player) {
            player.fallDistance = 0;
            if (!player.isSneaking()) {
                handleVelocityModification(player, entity -> entity.addVelocity(0, 0.4 + alpinistLevel * 0.1, 0), false);
            }
        }

        user.clearActiveItem();
        if (user instanceof PlayerEntity player) {
            int cooldown = 2;
            if (remainingUseTicks <= 1) {
                cooldown = this.getMaxUseTime(stack) / 2;
            } else if (remainingUseTicks > this.getMaxUseTime(stack) - this.earlyReleasePunishmentTime) {
                cooldown = remainingUseTicks / 4;
            }
            player.getItemCooldownManager().set(this, cooldown);
        }
    }

    @Nullable
    public static Vec3d getHookPos(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.POS)) return null;
        return Vec3dHelper.fromNbt(nbt.getCompound(NeMuelchNbtKeys.POS));
    }

    public static void setHookPos(ItemStack stack, @Nullable Vec3d pos) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (pos == null) {
                nbt.remove(NeMuelchNbtKeys.POS);
                return;
            }
        } else if (pos == null) {
            return;
        } else {
            nbt = stack.getOrCreateNbt();
        }
        NbtCompound hookPosNbt = new NbtCompound();
        Vec3dHelper.toNbt(hookPosNbt, pos);
        nbt.put(NeMuelchNbtKeys.POS, hookPosNbt);
    }

    public static Optional<Double> getHookDistance(ItemStack stack) {
        return Optional.ofNullable(stack.getNbt())
                .filter(nbt -> nbt.contains(NeMuelchNbtKeys.RADIUS))
                .map(nbt -> nbt.getDouble(NeMuelchNbtKeys.RADIUS));
    }

    public static void setHookDistance(ItemStack stack, double distance) {
        if (distance < 0) {
            if (stack.getNbt() != null) {
                stack.getNbt().remove(NeMuelchNbtKeys.RADIUS);
            }
            return;
        }
        stack.getOrCreateNbt().putDouble(NeMuelchNbtKeys.RADIUS, distance);
    }

    public static int getHookedDuration(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.USAGE_TICKS)) return 0;
        return nbt.getInt(NeMuelchNbtKeys.USAGE_TICKS);
    }

    public static void setHookedDuration(ItemStack stack, int duration) {
        if (duration <= 0) {
            if (stack.getNbt() != null) {
                stack.getNbt().remove(NeMuelchNbtKeys.USAGE_TICKS);
            }
            return;
        }
        stack.getOrCreateNbt().putInt(NeMuelchNbtKeys.USAGE_TICKS, duration);
    }

    public static int getAlpinistEnchantmentLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(NeMuelchEnchantments.ALPINIST, stack);
    }

    public static boolean hasSphericityEnchantment(ItemStack stack) {
        return EnchantmentHelper.getLevel(NeMuelchEnchantments.SPHERICITY, stack) > 0;
    }

    public static float getNormalizedSoaringEnchantmentLevel(ItemStack stack) {
        float level = EnchantmentHelper.getLevel(NeMuelchEnchantments.SOARING, stack);
        if (level <= 0) return 0;
        return MathHelper.clamp(level / NeMuelchEnchantments.SOARING.getMaxLevel(), 0f, 1f);
    }

    @SuppressWarnings("SameParameterValue")
    private static BlockHitResult raycast(World world, LivingEntity entity, double distance) {
        Vec3d start = entity.getEyePos();
        Vec3d end = start.add(entity.getRotationVec(1f).multiply(distance));
        RaycastContext raycastContext = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                entity
        );
        return world.raycast(raycastContext);
    }

    private static boolean isHookObstructed(World world, LivingEntity user, Vec3d hookPos, Vec3d fromEyePos) {
        RaycastContext raycastContext = new RaycastContext(fromEyePos, hookPos,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, user);
        BlockHitResult result = world.raycast(raycastContext);
        return result.getType() == HitResult.Type.BLOCK && result.getPos().squaredDistanceTo(hookPos) > 0.01;
    }

    private static double horizontalDistance(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static void createParticles(World world, @Nullable BlockHitResult hitResult) {
        if (hitResult == null || !world.isClient() || hitResult.getType() != HitResult.Type.BLOCK) return;
        Random random = world.getRandom();
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        Vec3d spawnPos = hitResult.getPos();
        Direction direction = hitResult.getSide();
        Vec3i directionVec = direction.getVector();
        double sprayDirectionMultiplier = 0.5;
        for (int i = 0; i < random.nextInt(40) + 20; i++) {
            world.addParticle(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    directionVec.getX() * sprayDirectionMultiplier,
                    directionVec.getY() * sprayDirectionMultiplier,
                    directionVec.getZ() * sprayDirectionMultiplier
            );
        }
    }

    private static void handleVelocityModification(LivingEntity entity, Consumer<LivingEntity> velocityChanger, boolean resetFall) {
        if (entity.getWorld().isClient()) return;
        velocityChanger.accept(entity);
        entity.velocityDirty = true;
        if (resetFall) entity.fallDistance = 0;
        for (ServerPlayerEntity packetTarget : PlayerLookupUtil.trackingAndSelf(entity)) {
            packetTarget.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
        }
    }

    public boolean canStartClimbing(World world, LivingEntity user, ItemStack stack) {
        return raycast(world, user, getModifiedMaxRange(stack)).getType() == HitResult.Type.BLOCK;
    }
}
