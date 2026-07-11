package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.ProjectileRicochetComponent;
import net.shirojr.nemuelch.init.NeMuelchEnchantments;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity implements Ownable {
    private ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "setOwner",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;owner:Lnet/minecraft/entity/Entity;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void addRicochetCount(Entity owner, CallbackInfo ci) {
        if (!(owner instanceof LivingEntity livingEntity)) return;
        ItemStack shootingStack = livingEntity.getMainHandStack();
        int level = EnchantmentHelper.getLevel(NeMuelchEnchantments.RICOCHET, shootingStack);
        if (level <= 0) return;
        ProjectileRicochetComponent component = ProjectileRicochetComponent.get((ProjectileEntity) (Object) this);
        component.setRicochetsLeft(level);
    }

    @WrapOperation(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onBlockHit(Lnet/minecraft/util/hit/BlockHitResult;)V"))
    private void onBlockHitWithRicochet(ProjectileEntity instance, BlockHitResult blockHitResult, Operation<Void> original) {
        ProjectileRicochetComponent component = ProjectileRicochetComponent.get(instance);
        if (component.getRicochetsLeft() <= 0 || instance.getVelocity().lengthSquared() <= 0.0001) {
            original.call(instance, blockHitResult);
            return;
        }
        component.handleReflection(blockHitResult);
    }
}
