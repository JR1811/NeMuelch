package net.shirojr.nemuelch.item.custom.armorAndShieldItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FortifiedShieldItem extends NeMuelchShield {
    public FortifiedShieldItem(ToolMaterial material) {
        super(material);
    }


    @Override
    public Text getName() {
        return Text.translatable("item.nemuelch.fortifiedshield");
    }

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

            this.damage(world.getDamageSources().magic());
            user.getItemCooldownManager().set(this, 200);
            return TypedActionResult.consume(user.getStackInHand(hand));
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
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
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.fortifiedshield.tooltip.shift"));
        } else {
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
