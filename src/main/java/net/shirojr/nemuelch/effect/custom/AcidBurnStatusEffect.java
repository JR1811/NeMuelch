package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.shirojr.nemuelch.event.custom.AcidCallbacks;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;

import java.util.List;
import java.util.function.Predicate;

public class AcidBurnStatusEffect extends StatusEffect {
    public static final Predicate<ItemStack> CLEARS_ACID_ON_CONSUMPTION = stack -> {
        if (PotionUtil.getPotion(stack).equals(Potions.WATER)) return true;
        if (stack.isIn(NeMuelchTags.Items.CLEARS_ACID_ON_CONSUMPTION)) return true;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(NeMuelchNbtKeys.ACID_CLEARER_NBT_KEY) && nbt.getBoolean(NeMuelchNbtKeys.ACID_CLEARER_NBT_KEY);
    };

    public AcidBurnStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 100 >> amplifier;
        return interval == 0 || duration % interval == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        entity.damage(NeMuelchDamageTypes.of(serverWorld, NeMuelchDamageTypes.ACID_BURN), 2f);

        GameRules gameRules = serverWorld.getGameRules();
        if (gameRules.getBoolean(NemuelchGameRules.ACID_CLEARS_BENEFICIAL_EFFECTS)) {
            List<StatusEffect> toRemove = entity.getStatusEffects().stream()
                    .map(StatusEffectInstance::getEffectType)
                    .filter(StatusEffect::isBeneficial).toList();
            toRemove.forEach(entity::removeStatusEffect);
        }
        double spreadDistance = gameRules.get(NemuelchGameRules.ACID_STATUS_EFFECT_SPREAD_DISTANCE).get();
        if (spreadDistance > 0) {
            StatusEffectInstance originAcidInstance = entity.getStatusEffect(this);
            if (originAcidInstance != null) {
                List<Entity> nearbyEntities = serverWorld.getOtherEntities(entity, entity.getBoundingBox().expand(spreadDistance));
                for (Entity otherEntity : nearbyEntities) {
                    if (!(otherEntity instanceof LivingEntity target)) continue;
                    if (AcidCallbacks.IS_DIRECT_CONTACT_PROTECTED.invoker().isContactProtected(target)) continue;
                    target.addStatusEffect(new StatusEffectInstance(this,
                            originAcidInstance.getDuration() / 2, originAcidInstance.getAmplifier()));
                }
            }
        }

    }
}
