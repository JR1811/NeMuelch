package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.projectile.SlimeItemEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.util.ItemStackUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("UnnecessaryReturnStatement")
@Debug(export = true)
@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void itemUseAdjustments(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (stack.getItem() instanceof ShearsItem) {
            if (!(user instanceof ServerPlayerEntity serverPlayer) || !serverPlayer.isSneaking()) return;
            Random random = serverPlayer.getServerWorld().getRandom();
            float pitch = MathHelper.lerp(random.nextFloat(), 0.7f, 1.3f);

            if (!serverPlayer.isCreative()) {
                stack.damage(1, random, serverPlayer);
            }
            serverPlayer.getServerWorld().playSound(null, user.getBlockPos(), NeMuelchSounds.SHEARS_SNAP,
                    SoundCategory.PLAYERS, 2f, pitch);
            cir.setReturnValue(TypedActionResult.success(stack));
            return;
        }

        if (stack.isOf(Items.SLIME_BALL)) {
            if (world instanceof ServerWorld serverWorld) {
                SlimeItemEntity slimeBallEntity = new SlimeItemEntity(serverWorld, user);
                slimeBallEntity.setItem(stack);
                slimeBallEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 0.65f, 3.0f);
                serverWorld.spawnEntity(slimeBallEntity);
                if (!user.isCreative()) {
                    stack.decrement(1);
                }
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_SLIME_BLOCK_PLACE,
                        SoundCategory.NEUTRAL, 0.5f, 0.75f);
                user.getItemCooldownManager().set(stack.getItem(), 60);
            }
            cir.setReturnValue(TypedActionResult.success(stack, world.isClient()));
            return;
        }

        if (stack.isOf(Items.STICK)) {
            if (!user.isOnFire()) return;
            if (world instanceof ServerWorld serverWorld) {
                if (serverWorld.getRandom().nextFloat() < 0.3f) {
                    user.setOnFire(false);
                }
                ItemStackUtils.igniteTorch(stack, user.getBlockPos(), user, serverWorld);
            }
            cir.setReturnValue(TypedActionResult.success(stack, world.isClient()));
            return;
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void itemUseOnBlockAdjustments(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(context.getBlockPos());

        if (stack.isOf(Items.STICK)) {
            if (player == null) return;
            if (!NeMuelchConfigInit.CONFIG.campfireUtilities) return;
            if (state.contains(Properties.LIT) && !state.get(Properties.LIT)) return;
            if (!state.isIn(NeMuelchTags.Blocks.TORCH_IGNITING_BLOCKS)) return;
            if (world instanceof ServerWorld serverWorld) {
                ItemStackUtils.igniteTorch(stack, context.getBlockPos(), player, serverWorld);
            }
            cir.setReturnValue(ActionResult.success(world.isClient()));
            return;
        }
    }

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void itemUseOnEntityAdjustments(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (stack.isOf(Items.STICK)) {
            if (!entity.isOnFire()) return;
            if (user.getWorld() instanceof ServerWorld serverWorld) {
                if (serverWorld.getRandom().nextFloat() < 0.3f) {
                    user.setOnFire(false);
                }
                ItemStackUtils.igniteTorch(stack, entity.getBlockPos(), user, serverWorld);
            }
            cir.setReturnValue(ActionResult.success(user.getWorld().isClient()));
            return;
        }
    }

    @Inject(method = "appendTooltip", at = @At("TAIL"))
    private void appendAdditionalTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (stack.getItem() instanceof ShearsItem) {
            tooltip.add(Text.translatable("item.nemuelch.shear_snap"));
        }
    }
}
