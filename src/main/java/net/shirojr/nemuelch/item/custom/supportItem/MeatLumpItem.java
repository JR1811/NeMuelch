package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class MeatLumpItem extends Item {
    private final State state;

    public MeatLumpItem(Settings settings, State state) {
        super(settings);
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (world instanceof ServerWorld) {
            if (this.state.equals(State.ROTTEN)) {
                if (world.getRandom().nextBoolean()) {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));
                } else {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600, 1));
                    if (!user.isUndead()) {
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600, 0));
                    }
                }
            } else if (this.state.equals(State.DEFAULT)) {
                if (world.getRandom().nextFloat() <= 0.1f) {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 1));
                }
            }
        }
        return result;
    }

    public enum State {
        DEFAULT, COOKED, ROTTEN
    }
}
