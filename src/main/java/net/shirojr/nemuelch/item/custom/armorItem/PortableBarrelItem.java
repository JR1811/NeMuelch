package net.shirojr.nemuelch.item.custom.armorItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortableBarrelItem extends ArmorItem implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);
    public static final String NBT_KEY_FILL_STATUS = "fill_status";
    public static final String NBT_KEY_WATER_PURITY = "fill_purity";    // 0 = dirty water, 1 = impurified water, 2 = purified water


    private static int max_fill = 10;

    public PortableBarrelItem(ArmorMaterial material, EquipmentSlot slot, Settings settings) {
        super(material, slot, settings);
    }

    //region animation
    // Predicate runs every frame
    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {

        LivingEntity livingEntity = event.getExtraDataOfType(LivingEntity.class).get(0);

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.idle", true));

        if (livingEntity instanceof ArmorStandEntity) {
            return PlayState.CONTINUE;
        }

        else if (livingEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) livingEntity;

            // Get all the equipment, aka the armor, currently held item, and offhand item
            List<Item> equipmentList = new ArrayList<>();
            player.getItemsEquipped().forEach((x) -> equipmentList.add(x.getItem()));

            // elements 2 to 6 are the armor so the 4th entry is the chest slot
            List<Item> armorList = equipmentList.subList(4, 5);

            // Make sure the player is wearing all the armor. If they are, continue playing
            // the animation, otherwise stop
            boolean isWearingAll = armorList.containsAll(Arrays.asList(NeMuelchItems.PORTABLE_BARREL));
            return isWearingAll ? PlayState.CONTINUE : PlayState.STOP;
        }
        return PlayState.STOP;
    }

    // All you need to do here is add your animation controllers to the
    // AnimationData
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 20, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
    //endregion

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(!world.isClient()) {
            if(entity instanceof PlayerEntity player) {
                ItemStack chestStack = player.getInventory().getArmorStack(2);

                if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL){
                    if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                                100, 1, true, false));
                    }
                }

                else if (stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) > 0){
                    if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                                100, 2, true, false));
                    }
                    if (!player.hasStatusEffect(StatusEffects.NAUSEA)) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,
                                300, 0, true, false));
                    }
                }
            }

        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            if (user.getStackInHand(hand).getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) > 0) {
                user.getStackInHand(hand).getOrCreateNbt().putInt(NBT_KEY_FILL_STATUS, 0);
                user.getStackInHand(hand).getOrCreateNbt().putInt(NBT_KEY_WATER_PURITY, 2);
            }
            if (world.isClient()) user.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.portable_barrel.tooltip.shift"));
        }

        else {
            tooltip.add(new TranslatableText("item.nemuelch.portable_barrel.tooltip.expand"));
            LiteralText status = new LiteralText("[" + stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) + "/" + ConfigInit.CONFIG.portableBarrelMaxFill + "] ");

            if (stack.getOrCreateNbt().getInt(NBT_KEY_WATER_PURITY) == 0 &&
                    stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) == 0) {

                stack.getOrCreateNbt().putInt(NBT_KEY_FILL_STATUS, 0);
                stack.getOrCreateNbt().putInt(NBT_KEY_WATER_PURITY, 2);
            }

            Text quality = switch (stack.getOrCreateNbt().getInt(NBT_KEY_WATER_PURITY)) {
                case 0 -> new TranslatableText("item.nemuelch.portable_barrel.tooltip.dirty");
                case 1 -> new TranslatableText("item.nemuelch.portable_barrel.tooltip.impure");
                default -> new TranslatableText("item.nemuelch.portable_barrel.tooltip.pure");
            };
            tooltip.add(status.append(quality));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }

    public static boolean isPortableBarrelEmpty(ItemStack chestStack) {
        return chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) <= 0;
    }

    public static boolean isPortableBarrelFull(ItemStack chestStack) {
        boolean isFull = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) >= ConfigInit.CONFIG.portableBarrelMaxFill;
        if (isFull) chestStack.getOrCreateNbt().putInt(NBT_KEY_FILL_STATUS, ConfigInit.CONFIG.portableBarrelMaxFill);   // clean-up
        return isFull;
    }
}
