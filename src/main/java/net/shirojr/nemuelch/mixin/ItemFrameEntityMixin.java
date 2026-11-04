package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.util.FoodModificationEntry;
import net.shirojr.nemuelch.init.NeMuelchGameRules;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.misc.FoodItemStackModificator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {
    @Shadow
    public abstract ItemStack getHeldItemStack();

    @Shadow
    public abstract void setHeldItemStack(ItemStack stack);

    private ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
        super(type, world, pos);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void mixPotionIntoStack(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (hand == Hand.OFF_HAND) return;
        if (player.isSpectator()) return;
        ItemStack storedStack = getHeldItemStack();
        ItemStack handStack = player.getMainHandStack();
        if (storedStack.isEmpty() || handStack.isEmpty()) return;
        if (!storedStack.getItem().isFood()) return;
        Potion potion = PotionUtil.getPotion(handStack);
        if (potion.equals(Potions.EMPTY)) return;

        if (!(getWorld() instanceof ServerWorld serverWorld)) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }
        double delayItemPickupRange = serverWorld.getGameRules().get(NeMuelchGameRules.FOOD_MODIFICATION_DELAY_PICKUP_RANGE).get();
        int delayItemIncrementSeconds = serverWorld.getGameRules().getInt(NeMuelchGameRules.FOOD_MODIFICATION_DELAY_INCREMENT_SECONDS);
        int delayItemMaxPickup = serverWorld.getGameRules().getInt(NeMuelchGameRules.FOOD_MODIFICATION_DELAY_MAX_SECONDS) / delayItemIncrementSeconds;
        double successRate = FoodItemStackModificator.getNormalizedAlchemistsSuccessRate(storedStack, serverWorld, this.getBlockPos());
        if (successRate > 0) {
            if (this.random.nextDouble() < successRate) {
                FoodItemStackModificator.mix(
                        storedStack,
                        FoodModificationEntry.create(potion, player),
                        this.consumeNearbyDelayItems(serverWorld, this.getPos(), delayItemPickupRange, delayItemMaxPickup) * (delayItemIncrementSeconds * 20)
                );
                this.setHeldItemStack(storedStack);
                serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION, SoundCategory.BLOCKS, 2f, 0.8f);
            }

            if (!player.isCreative()) {
                handStack.decrement(1);
                player.getInventory().offerOrDrop(Items.GLASS_BOTTLE.getDefaultStack());
            }
        }

        cir.setReturnValue(ActionResult.FAIL);
    }

    @SuppressWarnings("unused")
    @Unique
    private int consumeNearbyDelayItems(ServerWorld world, Vec3d origin, double range) {
        return this.consumeNearbyDelayItems(world, origin, range, Integer.MAX_VALUE);
    }

    @Unique
    private int consumeNearbyDelayItems(ServerWorld world, Vec3d origin, double range, int maxCount) {
        Box scanBox = new Box(origin, origin).expand(range);
        List<? extends ItemEntity> delayItems = world.getEntitiesByType(EntityType.ITEM, scanBox, itemEntity -> {
            if (!itemEntity.getStack().isIn(NeMuelchTags.Items.FOOD_MODIFICATION_DELAY_INGREDIENTS)) return false;
            if (origin.squaredDistanceTo(itemEntity.getPos()) > range * range) return false;
            BlockHitResult hitResult = world.raycast(new RaycastContext(
                    origin, itemEntity.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, itemEntity)
            );
            return hitResult.getType() == HitResult.Type.MISS;
        });

        int count = 0;
        int remaining = maxCount;
        for (ItemEntity entry : delayItems) {
            ItemStack stack = entry.getStack();
            int taken = Math.min(stack.getCount(), remaining);
            if (taken >= stack.getCount()) entry.discard();
            else entry.setStack(stack.copyWithCount(stack.getCount() - taken));
            count += taken;
            remaining -= taken;
            world.playSound(
                    null, entry.getPos().x, entry.getPos().y, entry.getPos().z,
                    SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS,
                    2f, MathHelper.lerp(this.random.nextFloat(), 0.85f, 1.1f)
            );
            if (remaining <= 0) break;
        }
        return MathHelper.clamp(count, 0, maxCount);
    }
}
