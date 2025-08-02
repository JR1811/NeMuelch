package net.shirojr.nemuelch.item.custom.gloveItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchToolMaterials;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrainingGloveItem extends SwordItem {
    public static final String NBT_KEY_GLOVE_HIT = "glove_hit";

    public TrainingGloveItem(Settings settings) {
        super(NeMuelchToolMaterials.GLOVE_LEATHER, 0, NeMuelchConfigInit.CONFIG.trainingGloveAttackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (target instanceof PlayerEntity enemyPlayer) {

            // assign glove stack
            ItemStack targetGloveStack;
            if (enemyPlayer.getMainHandStack().getItem() == stack.getItem()) {
                targetGloveStack = enemyPlayer.getMainHandStack();
            } else if (enemyPlayer.getOffHandStack().getItem() == stack.getItem()) {
                targetGloveStack = enemyPlayer.getOffHandStack();
            } else {
                return super.postHit(stack, target, attacker);
            }

            // regenerating only if enemy has the same item equipped
            NbtCompound enemyGloveNbt = targetGloveStack.getOrCreateNbt();

            if (!enemyGloveNbt.contains(NBT_KEY_GLOVE_HIT)) {
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, 1);
            } else {
                int oldHitValue = enemyGloveNbt.getInt(NBT_KEY_GLOVE_HIT);
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, oldHitValue + 1);
            }

            // points reached critical amount
            if (enemyGloveNbt.getInt(NBT_KEY_GLOVE_HIT) > NeMuelchConfigInit.CONFIG.trainingGloveMaxHits) {

                target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,
                        100, 0, true, false));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,
                        40, 1, true, false));

                target.playSound(SoundEvents.BLOCK_BELL_RESONATE, 2f, 1f);
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, 0);
            }

            target.heal(target.getMaxHealth() - target.getHealth());
        }

        target.playSound(SoundEvents.BLOCK_WOOL_PLACE, 1f, 1f);

        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        MutableText description = Text.translatable("item.nemuelch.training_glove.description");
        MutableText counter = Text.literal("§e" + stack.getOrCreateNbt().getInt(NBT_KEY_GLOVE_HIT) + "§r");
        tooltip.add(description.append(counter));
    }
}
