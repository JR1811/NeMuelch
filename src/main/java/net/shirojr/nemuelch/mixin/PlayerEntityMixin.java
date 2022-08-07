package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.Iterator;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    public PlayerEntityMixin(World world, PlayerEntity player) {
        super(EntityType.PLAYER, world);
    }

    // Thanks to 🕊 Aquaglyph 🕊#7209 on the fabric discord for helping out with the mixin
    @ModifyVariable(method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "LOAD",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockback(Lnet/minecraft/entity/LivingEntity;)I",
                    id = "i"))
    public int knockBackValue(int i) {
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
}