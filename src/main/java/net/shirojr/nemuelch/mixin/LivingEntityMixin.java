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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.init.ConfigInit;
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

    // extreme version of bad weather flying block
    /*    @Inject(method = "tickFallFlying", at = @At("HEAD"), cancellable = true)
    private void nemuelch$cancelFallFlying(CallbackInfo info) {
        World world = this.getWorld();

        if (!world.isClient && world.isRaining() && ConfigInit.CONFIG.blockBadWeatherFlying) {
            this.setFlag(FALL_FLYING_FLAG_INDEX, false);
            info.cancel();
        }
    }*/

    @Redirect(method = "travel",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFallFlying()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")
    )
    private void nemuelch$badWeatherFlying(LivingEntity instance, Vec3d vec3d) {
        World world = instance.getWorld();
        if (!(instance instanceof PlayerEntity player)) return;

        if (!optimalFlyingCondition(world, player) && ConfigInit.CONFIG.blockBadWeatherFlying) {
            NeMuelch.devLogger("applying bad condition flight | " + world);
            player.sendMessage(new TranslatableText("chat.nemuelch.bad_flying_condition"), true);
            Vec3d downForce = new Vec3d(0.0, -0.05, 0.0);
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
        int safeBlockHeight = ConfigInit.CONFIG.safeBlockHeightWhenBadWeather;

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
}
