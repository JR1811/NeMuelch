package net.shirojr.nemuelch.mixin;

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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void nemuelch$avoidDamageByEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        LivingEntity victim = ((LivingEntity)(Object)this);

        boolean isOfDamageSources = source.isProjectile() || source.isMagic() || source.isExplosive() ||
                source.isFallingBlock() || source.isFromFalling() || source.isFire();

        if (victim.hasStatusEffect(NeMuelchEffects.SHIELDING_SKIN) && isOfDamageSources) {
            victim.getWorld().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1f, 1f);

            info.setReturnValue(false);
        }
    }

    // extreme version of bad weather flying block
    /*    @Inject(method = "tickFallFlying", at = @At("HEAD"), cancellable = true)
    private void nemuelch$cancelFallFlying(CallbackInfo info) {
        World world = this.getWorld();

        if (!world.isClient && world.isRaining() && ConfigInit.CONFIG.blockbadWeatherFlying) {
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
        if (world.isThundering() || world.isRaining() && ConfigInit.CONFIG.blockBadWeatherFlying) {
            Vec3d downForce = new Vec3d(0.0, -0.05, 0.0);
            instance.setVelocity(vec3d.add(downForce));
        } else {
            instance.setVelocity(vec3d);
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
}
