package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rpgz.access.InventoryAccess;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FallenGuardArmorSetItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean blockedByShield(DamageSource source);

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Shadow
    public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow
    public abstract boolean isClimbing();

    @Shadow
    protected abstract boolean isImmobile();

    @Shadow private @Nullable LivingEntity attacker;

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
        if (source.getAttacker() instanceof PlayerEntity && NeMuelchConfigInit.CONFIG.specialPlayerLoot) {
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

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void nemuelch$onDeath(DamageSource source, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof LivingEntity attackerEntity)) return;
        if (!FallenGuardArmorSetItem.isFullyEquipped(attackerEntity)) return;
        //TODO: count kill quest
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

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isImmobile()Z"))
    private boolean preventImmobileState(boolean original) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return original && ((InventoryAccess) entity).getInventory().isEmpty();
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canMoveVoluntarily()Z", ordinal = 1))
    private boolean preventAiTicking(boolean original) {
        return original && !isImmobile();
    }
}
