package net.shirojr.nemuelch.item.custom.armorAndShieldItem;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class FallenGuardArmorSetItem extends ArmorItem implements IAnimatable {
    public final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public FallenGuardArmorSetItem(ArmorMaterial material, EquipmentSlot slot, Settings settings) {
        super(material, slot, settings);
    }

    public static boolean isFullyEquipped(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (!(stack.getItem() instanceof FallenGuardArmorSetItem)) return false;
        }
        return true;
    }

    public static boolean hasOneOrMoreEquipped(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack.getItem() instanceof FallenGuardArmorSetItem) return true;
        }
        return false;
    }

    @Override
    public void registerControllers(final AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "Controller",
                20, this::predicate));
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public enum BerserkQuest {

    }
}
