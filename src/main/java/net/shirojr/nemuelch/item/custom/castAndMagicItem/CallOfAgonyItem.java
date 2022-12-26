package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CallOfAgonyItem extends Item {
    private boolean successfulCast = true;

    public CallOfAgonyItem(Settings settings) {
        super(settings);
    }

    /*
    - setVelocity in a radius as knockback (look up e.g. CrookedCrooks mod)
    - particles in the vicinity
     */

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) {
            if (successfulCast) return TypedActionResult.success(itemStack);
            return TypedActionResult.pass(itemStack);
        }

        else {
            if (successfulCast) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 0, true, false));
                user.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.LEVITATING_ABSOLUTION, 80, 0, true, false));
                user.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.SHIELDING_SKIN, 100, 0, true, false));

                List<Entity> targets = world.getOtherEntities(user, Box.of(user.getPos(), 11, 6, 11));
                targets.forEach(entity -> {
                    //if (entity.isPlayer()) {

                        // 20% chance
                        if (world.random.nextInt(0, 9) < 0) {
                            ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(NeMuelchEffects.PLAYTHING_OF_THE_UNSEEN_DEITY, 70, 1));
                            ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 140, 1));
                        }
                        else {
                            // Calculates the vector used to pull the mob
                            Vec3d pos = user.getPos().subtract(entity.getPos());
                            pos = pos.subtract(user.getRotationVector());   //FIXME: invert the vector and increase strength if closer to caster

                            // This makes sure that the mob isn't simply flung
                            pos = pos.multiply(0.275);

                            entity.setVelocity(pos);
                            entity.velocityModified = true;
                            entity.fallDistance = 0.0F;
                        }
                    //}


                });

                NeMuelch.LOGGER.info("call of agony has been used");

                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        NeMuelchSounds.ITEM_RUNE, SoundCategory.PLAYERS, 1f, 1f);

                itemStack.decrement(1);
                return super.use(world, user, hand);
            }
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            if (false)   //TODO: implement knowledge system
            {
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description1"));
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description2"));
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description3"));
            }
            else {
                tooltip.add(new TranslatableText("item.nemuelch.rune.unknown"));
            }
        }
        else {
            tooltip.add(new TranslatableText("item.nemuelch.rune"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
