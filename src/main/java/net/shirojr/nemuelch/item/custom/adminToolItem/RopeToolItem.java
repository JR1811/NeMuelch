package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import net.shirojr.nemuelch.util.helper.NemuelchScreenOpener;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RopeToolItem extends Item implements ThirdPersonInvisible {
    public RopeToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 20;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        clearStoredPoint(user, stack);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        Optional<RopeData> hoveredRope = RopeToolItem.getHoveredRope(world, user);
        if (user.isSneaking() && hoveredRope.isPresent()) {
            if (world.isClient()) {
                NemuelchScreenOpener.openRopeModificationScreen(hoveredRope.get());
                getStoredPoint(stack).ifPresent(entry -> clearStoredPoint(user, stack));
            }
            return TypedActionResult.success(stack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Vec3d hitPos = context.getHitPos();
        ItemStack stack = context.getStack();
        Optional<Vec3d> storedPoint = getStoredPoint(stack);
        PlayerEntity user = context.getPlayer();
        if (user != null && user.isSneaking()) {
            return ActionResult.PASS;
        }

        if (storedPoint.isEmpty()) {
            setStoredPoint(stack, hitPos);
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        if (world instanceof ServerWorld) {
            RopesComponent component = RopesComponent.get(world);
            component.modifyRopes(true, ropeData -> ropeData.add(getDefaultOrPreset(storedPoint.get(), hitPos, stack)));
        }
        setStoredPoint(stack, null);
        return ActionResult.SUCCESS;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean clearStoredPoint(LivingEntity user, ItemStack stack) {
        Optional<Vec3d> storedPoint = getStoredPoint(stack);
        if (storedPoint.isPresent()) {
            if (user.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.NEUTRAL, 1f, 0.8f);
                setStoredPoint(stack, null);
            }
            return true;
        }
        return false;
    }

    public static Optional<Vec3d> getStoredPoint(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return Optional.empty();
        NbtCompound pointNbt = nbt.getCompound(NbtKeys.ROPE_MODIFICATION_POINT);
        if (pointNbt.isEmpty()) return Optional.empty();
        return Optional.ofNullable(Vec3dHelper.fromNbt(pointNbt));
    }

    public static void setStoredPoint(ItemStack stack, @Nullable Vec3d point) {
        if (point == null) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                nbt.remove(NbtKeys.ROPE_MODIFICATION_POINT);
            }
            return;
        }
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound pointNbt = new NbtCompound();
        Vec3dHelper.toNbt(pointNbt, point);
        nbt.put(NbtKeys.ROPE_MODIFICATION_POINT, pointNbt);
    }

    public RopeData getDefaultOrPreset(Vec3d posA, Vec3d posB, ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.contains(NbtKeys.ROPE_SEGMENTS) && nbt.contains(NbtKeys.ROPE_WIDTH) && nbt.contains(NbtKeys.ROPE_SLACK) && nbt.contains(NbtKeys.ROPE_IS_STABLE)) {
                return new RopeData(posA, posB, nbt.getInt(NbtKeys.ROPE_SEGMENTS), nbt.getFloat(NbtKeys.ROPE_WIDTH), nbt.getFloat(NbtKeys.ROPE_SLACK), nbt.getBoolean(NbtKeys.ROPE_IS_STABLE));
            }
        }
        return new RopeData(posA, posB, false);
    }

    public static void setPreset(ItemStack stack, int segments, float width, float slack, boolean isStable) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(NbtKeys.ROPE_SEGMENTS, segments);
        nbt.putFloat(NbtKeys.ROPE_WIDTH, width);
        nbt.putFloat(NbtKeys.ROPE_SLACK, slack);
        nbt.putBoolean(NbtKeys.ROPE_IS_STABLE, isStable);
    }

    public static void clearPreset(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        nbt.remove(NbtKeys.ROPE_SEGMENTS);
        nbt.remove(NbtKeys.ROPE_WIDTH);
        nbt.remove(NbtKeys.ROPE_SLACK);
        nbt.remove(NbtKeys.ROPE_IS_STABLE);
    }

    public static boolean isSettingsUsageBlocked(ServerPlayerEntity player, ItemStack stack, RopeData ropeData, double distance) {
        if (!(stack.getItem() instanceof RopeToolItem)) return true;
        if (player.isCreative() || player.hasPermissionLevel(2)) return false;
        return !(player.squaredDistanceTo(ropeData.pointA()) <= distance * distance) &&
                !(player.squaredDistanceTo(ropeData.pointB()) <= distance * distance);
    }

    public static Optional<RopeData> getHoveredRope(World world, LivingEntity originEntity) {
        if (!world.isClient()) return Optional.empty();
        RopesComponent component = RopesComponent.get(world);
        List<RopeData> ropes = component.getRopes();
        if (ropes.isEmpty()) return Optional.empty();

        Vec3d origin = originEntity.getEyePos();
        Vec3d lookDirection = originEntity.getRotationVec(1f);

        RopeData closest = null;
        double closestDist = 0.025;

        for (RopeData rope : ropes) {
            if (rope.isUnloaded(world)) continue;
            int samples = Math.max(8, rope.segments());
            Vec3d delta = rope.pointB().subtract(rope.pointA());

            for (int sampleIndex = 0; sampleIndex < samples; sampleIndex++) {
                float normalizedSample = (float) sampleIndex / samples;
                float nextNormalizedSample = (sampleIndex + 1f) / samples;

                Vec3d p0 = sampleRopeCurve(rope.pointA(), delta, rope.slack(), normalizedSample);
                Vec3d p1 = sampleRopeCurve(rope.pointA(), delta, rope.slack(), nextNormalizedSample);

                double angularDistance = angularDistanceToSegmentLine(origin, lookDirection, p0, p1);
                if (angularDistance < closestDist) {
                    closestDist = angularDistance;
                    closest = rope;
                }
            }
        }
        return Optional.ofNullable(closest);
    }

    public static Vec3d sampleRopeCurve(Vec3d posA, Vec3d delta, float slack, float normalizedSampleIndex) {
        double sag = slack * normalizedSampleIndex * (normalizedSampleIndex - 1.0F);
        double x = posA.x + delta.x * normalizedSampleIndex;
        double y = posA.y + (delta.y > 0
                ? delta.y * normalizedSampleIndex * normalizedSampleIndex
                : delta.y - delta.y * (1.0F - normalizedSampleIndex) * (1.0F - normalizedSampleIndex));
        y += sag;
        double z = posA.z + delta.z * normalizedSampleIndex;
        return new Vec3d(x, y, z);
    }

    private static double angularDistanceToSegmentLine(Vec3d origin, Vec3d lookDirection, Vec3d p0, Vec3d p1) {
        Vec3d originToP0 = p0.subtract(origin);
        Vec3d segment = p1.subtract(p0);

        double segmentLengthSq = segment.lengthSquared();
        if (segmentLengthSq == 0) {
            double projectedLengthToPoint = originToP0.dotProduct(lookDirection);
            if (projectedLengthToPoint <= 0) return Double.MAX_VALUE;
            return originToP0.subtract(lookDirection.multiply(projectedLengthToPoint)).length() / projectedLengthToPoint;
        }
        // closest points are where both points on their lines are perpendicular to each other
        double segmentDotLookDirection = segment.dotProduct(lookDirection);
        double originToP0DotSegment = originToP0.dotProduct(segment);
        double originToP0DotLookDirection = originToP0.dotProduct(lookDirection);

        double normalizedPointOnSegment = MathHelper.clamp(
                (segmentDotLookDirection * originToP0DotLookDirection - originToP0DotSegment) / (segmentLengthSq - segmentDotLookDirection * segmentDotLookDirection),
                0, 1
        );

        Vec3d closestPoint = p0.add(segment.multiply(normalizedPointOnSegment));
        Vec3d originToClosestPoint = closestPoint.subtract(origin);
        double projectedLengthToPoint = originToClosestPoint.dotProduct(lookDirection);
        if (projectedLengthToPoint <= 0) return Double.MAX_VALUE;

        return originToClosestPoint.subtract(lookDirection.multiply(projectedLengthToPoint)).length() / projectedLengthToPoint;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        getStoredPoint(stack).ifPresent(entry -> {
            tooltip.add(Text.translatable("tooltip.nemuelch.rope_modifier.line0", shortenDouble(entry.x), shortenDouble(entry.y), shortenDouble(entry.z)));
            tooltip.add(Text.empty());
        });
        tooltip.add(Text.translatable("tooltip.nemuelch.rope_modifier.line1"));
        tooltip.add(Text.translatable("tooltip.nemuelch.rope_modifier.line2"));
        tooltip.add(Text.translatable("tooltip.nemuelch.rope_modifier.line3"));
    }

    public static String shortenDouble(double value) {
        return String.format("%.2f", value);
    }
}
