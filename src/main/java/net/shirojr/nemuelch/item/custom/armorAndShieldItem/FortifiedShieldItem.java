package net.shirojr.nemuelch.item.custom.armorAndShieldItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.shirojr.nemuelch.init.ConfigInit;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.RawAnimation;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;
import java.util.function.Consumer;

public class FortifiedShieldItem extends NeMuelchShield implements IAnimatable {

    public AnimationFactory factory = new AnimationFactory(this);

    public FortifiedShieldItem(ToolMaterial material) {
        super(material);
    }

    //region animation
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.idle", false));

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    //endregion

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (user.isSneaking()) {
            List<Entity> targets = world.getOtherEntities(null, Box.of(user.getPos(), 11, 6, 11));
            targets.forEach(entity -> {
                if (entity instanceof PlayerEntity target) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));

                    target.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 2f, 1f);
                }
            });

            this.damage(DamageSource.MAGIC);
            user.getItemCooldownManager().set(this, 200);
            return TypedActionResult.consume(user.getStackInHand(hand));
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        stack.damage(1, miner, e -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
        });
        return true;

    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == Items.IRON_INGOT;    //FIXME: mby implement tags instead of hardcoding it
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.fortifiedshield.tooltip.shift"));
        }

        else {
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
