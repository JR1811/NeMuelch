package net.shirojr.nemuelch.item.custom.caneItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchItems;

public class GladiuscaneItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 80;

    public GladiuscaneItem(Settings settings) {
        super(settings);
    }

    //region effects on user
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity player) {

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player);
                }
            }
        }
    }

    private void applyEffect(PlayerEntity player) {
        boolean hasSlownessEffect = player.hasStatusEffect(StatusEffects.SLOWNESS);

        if (!hasSlownessEffect) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                    100, 0, true, false));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1f, 1f);

        NbtCompound nbtData = user.getStackInHand(hand).getOrCreateNbt().copy();

        ItemStack itemStack = new ItemStack(NeMuelchItems.GLADIUS_BLADE).copy();
        user.getItemCooldownManager().set(itemStack.getItem(), USE_COOLDOWN_TICKS);

        itemStack.setNbt(nbtData);

        user.setStackInHand(hand, itemStack);

        return TypedActionResult.success(itemStack, world.isClient());
    }
    //endregion


    //region effects on target
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA, 100, 1, false, false, false), attacker);

        return super.postHit(stack, target, attacker);
    }
    //endregion

}
