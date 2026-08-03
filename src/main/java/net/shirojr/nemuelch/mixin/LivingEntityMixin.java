package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rpgz.access.InventoryAccess;
import net.shirojr.nemuelch.compat.cca.component.BlightEntityComponent;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.effect.custom.DeferredInstantEffect;
import net.shirojr.nemuelch.effect.custom.ReboundEffect;
import net.shirojr.nemuelch.effect.util.UnremovableStatusEffectHolder;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.weaponry.NeMuelchShieldItem;
import net.shirojr.nemuelch.monster.abilities.custom.MultiJumpAbility;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import net.shirojr.nemuelch.util.duck.Generation;
import net.shirojr.nemuelch.util.helper.StatusEffectHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Debug(export = true)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, Generation, UnremovableStatusEffectHolder {
    @Unique
    private int generation;

    @Unique
    private final HashMap<StatusEffect, StatusEffectInstance> nemuelch_unremovableStatusEffectHolder = new HashMap<>();

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Shadow
    public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow
    public abstract boolean isClimbing();

    @Shadow
    protected abstract boolean isImmobile();

    @Shadow
    @Final
    private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow
    protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

    @Shadow
    public abstract @Nullable StatusEffectInstance removeStatusEffectInternal(@Nullable StatusEffect type);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public int nemuelch$getGeneration() {
        return this.generation;
    }

    @Override
    public void nemuelch$setGeneration(int generation) {
        this.generation = generation;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nemuelch$avoidDamageByEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        LivingEntity victim = ((LivingEntity) (Object) this);
        if (victim.hasStatusEffect(NeMuelchStatusEffects.SHIELDING_SKIN) && source.isIn(NeMuelchTags.DamageTypes.BLOCKED_BY_SHIELDING_SKIN_EFFECT)) {
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
        MonsterComponent monsterComponent = MonsterComponent.get(attacker);
        LivingEntity victim = (LivingEntity) (Object) this;
        monsterComponent.getAbilities().onKilledOther(attacker, victim);
    }

    @WrapOperation(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void blockFoodStackDecrement(ItemStack instance, int amount, Operation<Void> original) {
        if (instance.isIn(NeMuelchTags.Items.NO_FOOD_STACK_DECREMENT)) return;
        original.call(instance, amount);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canBreatheInWater()Z"))
    private void cleanseEntityBlight(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        BlightEntityComponent blightEntityComponent = BlightEntityComponent.get(entity);
        if (blightEntityComponent.isEmpty()) return;
        blightEntityComponent.clearSeverities(true);
    }

    @Inject(method = "sendPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/packet/Packet;)V"))
    private void handleBlightedItemPickup(Entity item, int count, CallbackInfo ci) {
        if (!(item instanceof ItemEntity itemEntity)) return;
        ItemStack stack = itemEntity.getStack();
        if (BlightType.hasNoStackBlight(stack)) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        for (BlightType type : BlightType.fromStack(stack)) {
            type.getActions().get().onPickedUp(entity, itemEntity, type);
        }
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private void onStatusEffectInstanceFinished(CallbackInfo ci, @Local StatusEffectInstance instance) {
        if (!(instance.getEffectType() instanceof DeferredInstantEffect deferredEffect)) return;
        deferredEffect.onFinishedDeference(instance, (LivingEntity) (Object) this);
    }

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z", ordinal = 3))
    private boolean ignoreCooldownForRebound(DamageSource instance, TagKey<DamageType> tag, Operation<Boolean> original) {
        MiscEntityComponent component = MiscEntityComponent.get((LivingEntity) (Object) this);
        if (!component.isApplyingRebound()) return original.call(instance, tag);
        return true;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void addToRebound(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.hasStatusEffect(NeMuelchStatusEffects.REBOUND)) return;
        MiscEntityComponent component = MiscEntityComponent.get(entity);
        component.getReboundDamages().offer(new ReboundEffect.DamageInstance(source, amount));
    }

    @WrapOperation(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getXpToDrop()I"))
    private int modifyXpDropForOccasions(LivingEntity instance, Operation<Integer> original) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get(getWorld());
        List<OccasionEntry> occasions = component.getUnsyncedActiveOccasions();
        Integer originalXp = original.call(instance);
        for (OccasionEntry occasion : occasions) {
            originalXp = occasion.getType().getModifiedXp(originalXp, (LivingEntity) (Object) this, nemuelch$getGeneration());
        }
        return originalXp;
    }

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    private void canHaveAcidStatusEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (!effect.getEffectType().equals(NeMuelchStatusEffects.ACID_BURN)) return;
        cir.setReturnValue(!this.getType().isIn(NeMuelchTags.EntityTypes.ACID_IMMUNE));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(NbtKeys.GENERATION)) {
            this.nemuelch$setGeneration(nbt.getInt(NbtKeys.GENERATION));
        } else {
            this.nemuelch$setGeneration(0);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt(NbtKeys.GENERATION, this.nemuelch$getGeneration());
    }

    @WrapMethod(method = "blockedByShield")
    private boolean blockedByCustomShields(DamageSource source, Operation<Boolean> original) {
        LivingEntity user = (LivingEntity) (Object) this;
        if (user.getActiveItem().getItem() instanceof NeMuelchShieldItem) {
            return NeMuelchShieldItem.blockedByCustomShield(user, source);
        }
        return original.call(source);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    private void onSuccessfulBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity user = (LivingEntity) (Object) this;
        if (user.getActiveItem().getItem() instanceof NeMuelchShieldItem shieldItem) {
            shieldItem.onSuccessfulBLock(user, source, amount);
        }
    }

    @WrapOperation(
            method = "tickMovement",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/LivingEntity;getSwimHeight()D"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z",
                    ordinal = 2
            )
    )
    private boolean allowMultiJump(LivingEntity instance, Operation<Boolean> original) {
        if (original.call(instance)) return true;
        MonsterComponent component = MonsterComponent.get(instance);
        return component.getAbilities().get(MultiJumpAbility.class).map(MultiJumpAbility::canMultiJump).orElse(false);
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onMultiJump(CallbackInfo ci) {
        if (!((LivingEntity) (Object) this instanceof PlayerEntity player)) return;
        if (player.isOnGround()) return;
        MonsterComponent component = MonsterComponent.get(player);
        component.getAbilities().get(MultiJumpAbility.class).ifPresent(multiJumpAbility -> multiJumpAbility.onMultiJumped(player));
    }

    @Inject(method = "clearStatusEffects", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private void unlistUnremovableEffects(CallbackInfoReturnable<Boolean> cir) {
        Iterator<Map.Entry<StatusEffect, StatusEffectInstance>> iterator = this.activeStatusEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<StatusEffect, StatusEffectInstance> entry = iterator.next();
            if (StatusEffectHelper.isIn(entry.getValue().getEffectType(), NeMuelchTags.StatusEffects.UNREMOVABLE_EFFECTS)) {
                this.nemuelch_unremovableStatusEffectHolder.put(entry.getKey(), entry.getValue());
                iterator.remove();
            }
        }
    }

    @Definition(id = "effectInstance", local = @Local(type = StatusEffectInstance.class))
    @Expression("effectInstance != null")
    @ModifyExpressionValue(method = "removeStatusEffect", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean avoidUnremovableStatusEffect(boolean original, @Local StatusEffectInstance statusEffectInstance) {
        if (StatusEffectHelper.isIn(statusEffectInstance.getEffectType(), NeMuelchTags.StatusEffects.UNREMOVABLE_EFFECTS)) {
            return false;
        }
        return original;
    }

    @Inject(method = "clearStatusEffects", at = @At(value = "RETURN"))
    private void relistUnremovableEffects(CallbackInfoReturnable<Boolean> cir) {
        if (this.nemuelch_unremovableStatusEffectHolder.isEmpty()) return;
        this.activeStatusEffects.putAll(this.nemuelch_unremovableStatusEffectHolder);
        this.nemuelch_unremovableStatusEffectHolder.clear();
    }

    @Override
    public boolean neMuelch$forceStatusEffectsClear() {
        boolean changed = false;
        Iterator<StatusEffectInstance> iterator = this.activeStatusEffects.values().iterator();
        while (iterator.hasNext()) {
            StatusEffectInstance entry = iterator.next();
            this.onStatusEffectRemoved(entry);
            iterator.remove();
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean neMuelch$forceStatusEffectRemoval(StatusEffect effect) {
        StatusEffectInstance statusEffectInstance = this.removeStatusEffectInternal(effect);
        if (statusEffectInstance != null) {
            this.onStatusEffectRemoved(statusEffectInstance);
            return true;
        } else {
            return false;
        }
    }
}
