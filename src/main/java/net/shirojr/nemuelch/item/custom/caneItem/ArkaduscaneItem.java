package net.shirojr.nemuelch.item.custom.caneItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;

public class ArkaduscaneItem extends Item implements IAnimatable {

    private static final int MAX_CHARGE = ConfigInit.CONFIG.arkadusCaneMaxCharge;
    private static final int USE_COOLDOWN_TICKS = 80;
    private static final ItemStack STACK_WHEN_NOT_CHARGED = new ItemStack(NeMuelchItems.PEST_CANE);

    public AnimationFactory factory = new AnimationFactory(this);

    // ctor
    public ArkaduscaneItem(Settings settings) {
        super(settings);
    }

    //region animation
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.arkaduscane.stickshift", false));

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
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, USE_COOLDOWN_TICKS);

        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1f, 1f);


        if (!itemStack.hasNbt()) {
            NbtCompound nbt = itemStack.getOrCreateNbt();
            nbt.putInt("arkaduscane_charge", MAX_CHARGE);
        }

        //region reload
        if (itemStack.getOrCreateNbt().getInt("arkaduscane_charge") < MAX_CHARGE) {

            if (Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(user.getStackInHand(Hand.OFF_HAND).getItem().asItem()).get()).isIn(NeMuelchTags.Items.ARKADUSCANE_PROJECTILE)) {
                user.getStackInHand(Hand.OFF_HAND).decrement(1);

                int oldCharge = itemStack.getOrCreateNbt().getInt("arkaduscane_charge");
                NbtCompound nbt = itemStack.getOrCreateNbt();
                nbt.putInt("arkaduscane_charge", oldCharge + 1);

                user.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1f, 1f);
                return TypedActionResult.success(itemStack, world.isClient());
            } else if (Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(user.getStackInHand(Hand.MAIN_HAND).getItem().asItem()).get()).isIn(NeMuelchTags.Items.ARKADUSCANE_PROJECTILE)) {
                user.getStackInHand(Hand.MAIN_HAND).decrement(1);

                int oldCharge = itemStack.getOrCreateNbt().getInt("arkaduscane_charge");
                NbtCompound nbt = itemStack.getOrCreateNbt();
                nbt.putInt("arkaduscane_charge", oldCharge + 1);

                user.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1f, 1f);
                return TypedActionResult.success(itemStack, world.isClient());
            }
        }
        //endregion

        if (itemStack.getOrCreateNbt().getInt("arkaduscane_charge") > 0) {

            //handle charge value
            int oldCharge = itemStack.getOrCreateNbt().getInt("arkaduscane_charge");

            itemStack.getOrCreateNbt().putInt("arkaduscane_charge", oldCharge - 1);

            // spawning entity
            if (!world.isClient()) {

                ArkaduscaneProjectileEntity projectileEntity = new ArkaduscaneProjectileEntity(user, world);
                projectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
                projectileEntity.setPos(user.getEyePos().getX(), user.getEyePos().getY(), user.getEyePos().getZ());

                world.spawnEntity(projectileEntity);
            }
        } else {

            itemStack = STACK_WHEN_NOT_CHARGED.copy();

            user.setStackInHand(hand, itemStack);
            user.playSound(SoundEvents.ITEM_SHIELD_BREAK, 1f, 1f);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
    //endregion

    //region arkaduscane effects on target
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA, 100, 1, false, false, false), attacker);

        return super.postHit(stack, target, attacker);
    }
    //endregion

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.arkaduscane.tooltip.shift.line1"));
            tooltip.add(new TranslatableText("item.nemuelch.arkaduscane.tooltip.shift.line2"));
        } else {
            tooltip.add(new TranslatableText("item.nemuelch.arkaduscane.tooltip.expand.line1"));
            tooltip.add(new LiteralText("[" + stack.getOrCreateNbt().getInt("arkaduscane_charge") + "/" + ConfigInit.CONFIG.arkadusCaneMaxCharge + "]"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
