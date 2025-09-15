package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rpgz.access.InventoryAccess;
import net.shirojr.nemuelch.compat.cca.component.monster.GeneralMonsterComponent;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Debug(export = true)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Shadow
    public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow
    public abstract boolean isClimbing();

    @Shadow
    protected abstract boolean isImmobile();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nemuelch$avoidDamageByEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        LivingEntity victim = ((LivingEntity) (Object) this);

        List<TagKey<DamageType>> blockedSources = List.of(DamageTypeTags.IS_PROJECTILE, DamageTypeTags.IS_EXPLOSION, DamageTypeTags.IS_FALL, DamageTypeTags.IS_FIRE);
        boolean isOfDamageSources = source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.FALLING_BLOCK) || blockedSources.stream().anyMatch(source::isIn);

        if (victim.hasStatusEffect(NeMuelchEffects.SHIELDING_SKIN) && isOfDamageSources) {
            victim.getWorld().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1f, 1f);

            info.setReturnValue(false);
        }
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void nemuelch$dropSpecializedLoot(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && NeMuelchConfigInit.CONFIG.specialPlayerLoot) {
            if (getUuidAsString().equals("39aa14b1-815b-4d67-b958-36e2e0971f9c")) {
                ItemStack stack = new ItemStack(Items.PUFFERFISH);
                NbtCompound nbtCompound = stack.getOrCreateSubNbt("display");
                nbtCompound.putString("Name", Text.Serializer.toJson(Text.translatable("loot.nemuelch.39aa14b1-815b-4d67-b958-36e2e0971f9c.name")));
                dropStack(stack);
            }
        }
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
        if (!(entity instanceof InventoryAccess inventoryAccess)) return original;
        return original && inventoryAccess.getInventory().isEmpty();
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canMoveVoluntarily()Z", ordinal = 1))
    private boolean preventAiTicking(boolean original) {
        return original && !isImmobile();
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;"))
    private void onDeathAdditions(DamageSource damageSource, CallbackInfo ci) {
        if (!(damageSource.getAttacker() instanceof LivingEntity attacker)) return;
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(attacker);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            LivingEntity victim = (LivingEntity) (Object) this;
            entry.getAbilities().onKilledOther(attacker, victim);
        }
    }
}
