package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.event.custom.ItemPickupCallbacks;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.ArtifactItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Ownable {
    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    public abstract void setStack(ItemStack stack);

    @Shadow
    public abstract @Nullable Entity getOwner();

    private ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;applyWaterBuoyancy()V"))
    private void cleanseItemBlight(CallbackInfo ci) {
        ItemStack stack = getStack();
        if (BlightType.hasNoStackBlight(stack)) return;
        BlightType.applyToStack(stack, Set.of());
        setStack(stack);
        //TODO: add flashy effects?
    }

    @Inject(
            method = "onPlayerCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V"
            )
    )
    private void callItemPickUpEvents(PlayerEntity player, CallbackInfo ci) {
        ItemPickupCallbacks.ON_ENTITY_PICKED_UP_ITEM.invoker().onEntityPickedUpItem(player, (ItemEntity) (Object) this);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onItemEntityDestroyed(Lnet/minecraft/entity/ItemEntity;)V"))
    private void applyCurse(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (getStack().getItem() instanceof ArtifactItem artifactItem) {
            artifactItem.onItemEntityKilled((ItemEntity) (Object) this, source, getOwner());
        }
    }
}
