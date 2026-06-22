package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.custom.weaponry.NeMuelchShieldItem;
import net.shirojr.nemuelch.util.duck.MobPersistency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter, MobPersistency {
    @Shadow
    private boolean persistent;

    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean nemuelch$isPersistent() {
        return persistent;
    }

    @Override
    public void nemuelch$setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @ModifyExpressionValue(method = "disablePlayerShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean isCustomShield(boolean original, @Local(ordinal = 1, argsOnly = true) ItemStack playerStack) {
        return original || NeMuelchShieldItem.isShieldItem(playerStack);
    }

    @WrapOperation(method = "disablePlayerShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    private void setCustomShieldCooldown(ItemCooldownManager instance, Item item, int duration, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player) {
        if (NeMuelchShieldItem.isShieldItem(activeItemStack)) {
            Item shieldItem = activeItemStack.getItem();
            int newDuration = shieldItem instanceof NeMuelchShieldItem customShieldItem
                    ? customShieldItem.getCooldownDuration(player, activeItemStack, true)
                    : duration;
            original.call(instance, shieldItem, newDuration);
        }
    }
}
