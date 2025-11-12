package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoapItem extends Item {
    public static final String SOAPED_NBT_KEY = "soaped";

    private final int maxCoatingCharges;

    /**
     * @param maxCoatingCharges use <code>-1</code> to apply infinite coating charges
     */
    public SoapItem(Settings settings, int maxCoatingCharges) {
        super(settings);
        this.maxCoatingCharges = maxCoatingCharges;
    }

    public int getMaxCoatingCharges() {
        return maxCoatingCharges;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.soap.tooltip"));
            if (hasInfiniteCoating(stack)) {
                tooltip.add(Text.translatable("item.nemuelch.creative_soap.tooltip"));
            }
        } else {
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack soapStack = user.getStackInHand(Hand.MAIN_HAND);
        ItemStack otherStack = user.getStackInHand(Hand.OFF_HAND);

        if (soapStack.getItem().equals(this) && canBeCoated(otherStack)) {
            if (getMaxCoatingCharges() == -1) {
                if (hasInfiniteCoating(otherStack)) return TypedActionResult.pass(soapStack);
                applyInfiniteCoating(otherStack);
            } else {
                if (hasInfiniteCoating(otherStack) || getCoatingCharges(otherStack) == getMaxCoatingCharges()) {
                    return TypedActionResult.pass(soapStack);
                }
                setCoating(otherStack, getMaxCoatingCharges());
                if (!user.isCreative()) {
                    soapStack.decrement(1);
                }
            }
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.playSound(null, user.getBlockPos(), SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.NEUTRAL, 1f, 1f);
            }
            return TypedActionResult.success(soapStack);
        }
        return super.use(world, user, hand);
    }

    public static boolean canBeCoated(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || stack.isIn(NeMuelchTags.Items.SOAP_COATABLE);
    }

    public static void setCoating(ItemStack stack, int charges) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (hasInfiniteCoating(stack) || !canBeCoated(stack)) return;
        if (charges == 0) {
            nbt.remove(SOAPED_NBT_KEY);
        } else {
            nbt.putInt(SOAPED_NBT_KEY, Math.max(charges, 0));
        }
    }

    public static boolean hasCoating(ItemStack stack) {
        return stack.getNbt() != null && stack.getNbt().contains(SOAPED_NBT_KEY);
    }

    @SuppressWarnings("unused")
    public static boolean hasCoating(LivingEntity entity) {
        return getFirstCoatedStack(entity) != null;
    }

    public static int getCoatingCharges(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SOAPED_NBT_KEY)) return 0;
        return nbt.getInt(SOAPED_NBT_KEY);
    }

    @Nullable
    public static ItemStack getFirstCoatedStack(LivingEntity entity) {
        for (ItemStack armorStack : entity.getArmorItems()) {
            if (hasCoating(armorStack)) return armorStack;
        }
        if (hasCoating(entity.getMainHandStack())) return entity.getMainHandStack();
        if (hasCoating(entity.getOffHandStack())) return entity.getOffHandStack();
        return null;
    }

    public static void decrementCoating(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SOAPED_NBT_KEY)) return;
        if (hasInfiniteCoating(stack)) return;
        int currentCoat = nbt.getInt(SOAPED_NBT_KEY);
        if (currentCoat <= 1) {
            stack.getNbt().remove(SOAPED_NBT_KEY);
        } else {
            stack.getNbt().putInt(SOAPED_NBT_KEY, currentCoat - 1);
        }
    }

    public static void applyInfiniteCoating(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.remove(SOAPED_NBT_KEY);
        nbt.putInt(SOAPED_NBT_KEY, -1);
    }

    public static boolean hasInfiniteCoating(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SOAPED_NBT_KEY)) return false;
        return nbt.getInt(SOAPED_NBT_KEY) == -1;
    }
}
