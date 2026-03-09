package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchEntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityLeashingMixin extends LivingEntity {
    private PlayerEntityLeashingMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "createPlayerAttributes", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;"))
    private static DefaultAttributeContainer.Builder appendEntityAttribute(Operation<DefaultAttributeContainer.Builder> original) {
        DefaultAttributeContainer.Builder attributeBuilder = original.call();
        attributeBuilder.add(NeMuelchEntityAttributes.BIND_RADIUS, NeMuelchEntityAttributes.BIND_RADIUS.getDefaultValue());
        return attributeBuilder;
    }

    @Inject(method = "interact", at = @At("RETURN"))
    private void interactLeash(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult returnValue = cir.getReturnValue();
        if (returnValue != ActionResult.PASS) return;
        if (!((PlayerEntity) (Object) this instanceof ServerPlayerEntity serverPlayer)) return;

    }
}
