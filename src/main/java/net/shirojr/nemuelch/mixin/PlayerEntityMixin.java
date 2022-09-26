package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    public PlayerEntityMixin(World world, PlayerEntity player) {
        super(EntityType.PLAYER, world);
    }

    //Thanks to 🕊 Aquaglyph 🕊#7209 on the fabric discord for helping out with the KnockBack mixin
    @ModifyVariable(method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "LOAD",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockback(Lnet/minecraft/entity/LivingEntity;)I",
                    id = "i"))
    public int applyDefaultKnockbackFromStack(int i) {
        ItemStack itemInMainHand = this.getEquippedStack(EquipmentSlot.MAINHAND);

        if (!itemInMainHand.isEmpty()) {

            Collection<EntityAttributeModifier> knockBackAttributes =
                    itemInMainHand.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);

            if (knockBackAttributes != null && knockBackAttributes.size() > 0) {

                Iterator<EntityAttributeModifier> iterator = knockBackAttributes.iterator();

                if (iterator.hasNext()) {
                    EntityAttributeModifier entityAttributeModifier = iterator.next();
                    double knockBackValue = entityAttributeModifier.getValue();

                    return i + (int) knockBackValue;
                }
            }
        }

        return i;
    }

    @Inject(method = "findRespawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), cancellable = true)
    private static void applyCustomCoordinatesRespawnPosition(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> info) {

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        boolean customBedRespawn = ConfigInit.CONFIG.useCustomBedRespawnLocation;

        double x = ConfigInit.CONFIG.respawnLocationX;
        double y = ConfigInit.CONFIG.respawnLocationY;
        double z = ConfigInit.CONFIG.respawnLocationZ;

        if (customBedRespawn && block instanceof BedBlock && BedBlock.isBedWorking(world)) {

            info.setReturnValue(Optional.of(new Vec3d(x + 0.5, y + 0.1, z + 0.5)));
            //info.cancel();
        }
    }
}