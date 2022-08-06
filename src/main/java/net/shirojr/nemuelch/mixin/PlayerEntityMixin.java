package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{

    private ItemStack stackWithKnockback;
    private ItemStack mainHandStack;
    private int knockbackValue;

    public PlayerEntityMixin(World world, PlayerEntity player) {

        super(EntityType.PLAYER, world);
    }

    @ModifyConstant(method = "attack", constant = @Constant(intValue = 0))
    private int attackInject(int value) {
        value = this.getStackInHand();


        if (mainHandStack == stackWithKnockback) {
            value += knockbackValue;
        }
        return value;
    }
}

    //TODO: createPlayerAttributes creates attributes; add the knockback attribute
    //TODO: attack initializes i to 0 instead of calling the getattribute; ModifyConstant that int i = 0 to get the attribute -> check for mainhandstack == cane