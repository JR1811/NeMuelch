package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean blockedByShield(DamageSource source);

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Shadow
    public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow public abstract boolean isClimbing();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nemuelch$avoidDamageByEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        LivingEntity victim = ((LivingEntity) (Object) this);

        boolean isOfDamageSources = source.isProjectile() || source.isMagic() || source.isExplosive() ||
                source.isFallingBlock() || source.isFromFalling() || source.isFire();

        if (victim.hasStatusEffect(NeMuelchEffects.SHIELDING_SKIN) && isOfDamageSources) {
            victim.getWorld().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1f, 1f);

            info.setReturnValue(false);
        }
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void nemuelch$dropSpecializedLoot(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && ConfigInit.CONFIG.specialPlayerLoot) {
            switch (getUuidAsString()) {
                case "39aa14b1-815b-4d67-b958-36e2e0971f9c":
                    ItemStack stack = new ItemStack(Items.PUFFERFISH);
                    NbtCompound nbtCompound = stack.getOrCreateSubNbt("display");
                    nbtCompound.putString("Name", Text.Serializer.toJson(new TranslatableText("loot.nemuelch.39aa14b1-815b-4d67-b958-36e2e0971f9c.name")));
                    dropStack(stack);
                    break;
            }
        }
    }

    @Redirect(method = "travel",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFallFlying()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")
    )
    private void nemuelch$badWeatherFlying(LivingEntity instance, Vec3d vec3d) {
        World world = instance.getWorld();
        if (!(instance instanceof PlayerEntity player)) return;
        if (player.getAbilities().creativeMode || player.isSpectator()) return;

        if (!optimalFlyingCondition(world, player) && ConfigInit.CONFIG.badWeatherFlyingBlock) {
            NeMuelch.devLogger("applying bad condition flight | " + world);
            player.sendMessage(new TranslatableText("chat.nemuelch.bad_flying_condition"), true);
            Vec3d downForce = new Vec3d(0.0, -(ConfigInit.CONFIG.badWeatherDownForce), 0.0);
            player.setVelocity(vec3d.add(downForce));
        } else {
            NeMuelch.devLogger("applying normal flight | " + world);
            player.setVelocity(vec3d);
        }
    }

    /**
     * Utility method which considers weather, safe flight height and a clear view into the sky.
     *
     * @param world
     * @param livingEntity
     * @return true, if flying condition are optimal.
     */
    @Unique
    private static boolean optimalFlyingCondition(World world, LivingEntity livingEntity) {
        BlockPos livingEntityPos = livingEntity.getBlockPos();
        int safeBlockHeight = ConfigInit.CONFIG.badWeatherSafeBlockHeight;

        if (!world.isThundering() && !world.isRaining()) return true;
        if (!world.isSkyVisible(livingEntityPos)) return true;

        boolean isSafeHeight = false;
        for (int i = 0; i < safeBlockHeight; i++) {
            if (world.getBlockState(livingEntityPos).getBlock() != Blocks.AIR) {
                isSafeHeight = true;
            }
            livingEntityPos = livingEntityPos.down();
        }
        return isSafeHeight;
    }

    /**
     * Implementation of Body Pull feature
     *
     * @param user Player who is about to pull the body
     * @param hand Hand, which is used to drag the body
     */
    @Override
    public ActionResult interactAt(PlayerEntity user, Vec3d hitPos, Hand hand) {
        NeMuelch.devLogger("mixin executed");

        ItemStack stack = user.getStackInHand(hand);
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (!(livingEntity instanceof PlayerEntity targetPlayer)) return super.interactAt(user, hitPos, hand);
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.interactAt(user, hitPos, hand);

        NeMuelch.devLogger("not on cooldown");

        boolean isTool = stack.getItem() instanceof ShovelItem || stack.isIn(NeMuelchTags.Items.PULL_BODY_TOOLS);
        if (!isTool) return super.interactAt(user, hitPos, hand);
        if (!targetPlayer.isDead()) return super.interactAt(user, hitPos, hand);

        NeMuelch.devLogger("targetPlayer is player and is dead");

        if (!user.getWorld().isClient()) {
            NeMuelch.devLogger("applying operations on server side: " + world);
            Vec3d pull = user.getPos().subtract(targetPlayer.getPos());
            pull.subtract(user.getRotationVector());

            targetPlayer.setVelocity(
                    pull.getX() * ConfigInit.CONFIG.pullBodyHorizontal, ConfigInit.CONFIG.pullBodyVertical,
                    pull.getZ() * ConfigInit.CONFIG.pullBodyHorizontal
            );
            targetPlayer.velocityModified = true;

            stack.damage(ConfigInit.CONFIG.pullToolDamage, user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(stack.getItem(), ConfigInit.CONFIG.pullToolCooldown);

            ServerWorld world = (ServerWorld) user.getWorld();
            world.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.PLAYERS,
                    2f, 1f);
        }

        return ActionResult.SUCCESS;
    }

    @Inject(at = @At("HEAD"), method = "applyClimbingSpeed", cancellable = true)
    private void nemuelch$applyScaffoldingMotion(Vec3d motion, CallbackInfoReturnable<Vec3d> cir) {
        if (this.isClimbing() && this.getBlockStateAtPos().isOf(NeMuelchBlocks.IRON_SCAFFOLDING)) {
            // The additional logic in applyClimbingSpeed only applies if the block isn't scaffolding
            this.onLanding();
            double x = MathHelper.clamp(motion.x, -0.15000000596046448, 0.15000000596046448);
            double z = MathHelper.clamp(motion.z, -0.15000000596046448, 0.15000000596046448);
            double y = Math.max(motion.y, -0.15000000596046448);

            cir.setReturnValue(new Vec3d(x, y, z));
        }
    }
}
