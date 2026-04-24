package net.shirojr.nemuelch.item.custom.weaponry;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableBarrelItem extends ArmorItem {
    public static final String NBT_KEY_FILL_STATUS = "fill_status";
    public static final String NBT_KEY_WATER_PURITY = "fill_purity";    // 0 = dirty water, 1 = impurified water, 2 = purified water


    private static final int max_fill = 10;

    public PortableBarrelItem(ArmorMaterial material, ArmorItem.Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity player) {
                ItemStack chestStack = player.getInventory().getArmorStack(2);

                // slowness when carried
                if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL) {
                    if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                                100, 1, true, false));
                    }
                }

                if (stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) > 0 && chestStack != stack) {
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
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.portable_barrel.tooltip.shift"));
        } else {
            tooltip.add(Text.translatable("item.nemuelch.portable_barrel.tooltip.expand"));
            MutableText status = Text.literal("[" + stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) + "/" + NeMuelchConfigInit.CONFIG.portableBarrelMaxFill + "] ");

            if (stack.getOrCreateNbt().getInt(NBT_KEY_WATER_PURITY) == 0 &&
                    stack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) == 0) {

                stack.getOrCreateNbt().putInt(NBT_KEY_FILL_STATUS, 0);
                stack.getOrCreateNbt().putInt(NBT_KEY_WATER_PURITY, 2);
            }

            Text quality = switch (stack.getOrCreateNbt().getInt(NBT_KEY_WATER_PURITY)) {
                case 0 -> Text.translatable("item.nemuelch.portable_barrel.tooltip.dirty");
                case 1 -> Text.translatable("item.nemuelch.portable_barrel.tooltip.impure");
                default -> Text.translatable("item.nemuelch.portable_barrel.tooltip.pure");
            };
            tooltip.add(status.append(quality));
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
    }

    public static boolean isPortableBarrelEmpty(ItemStack chestStack) {
        return chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) <= 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPortableBarrelFull(ItemStack chestStack) {
        boolean isFull = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) >= NeMuelchConfigInit.CONFIG.portableBarrelMaxFill;
        if (isFull)
            chestStack.getOrCreateNbt().putInt(NBT_KEY_FILL_STATUS, NeMuelchConfigInit.CONFIG.portableBarrelMaxFill);   // clean-up
        return isFull;
    }
}
