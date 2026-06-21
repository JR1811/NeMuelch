package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.CrystalBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class CrystalBlockItem extends BlockItem {
    public CrystalBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static OptionalInt getInnerColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NbtKeys.INNER_COLOR_NBT_KEY)) return OptionalInt.empty();
        return OptionalInt.of(nbt.getInt(NbtKeys.INNER_COLOR_NBT_KEY));
    }

    public static void setInnerColor(ItemStack stack, int color) {
        stack.getOrCreateNbt().putInt(NbtKeys.INNER_COLOR_NBT_KEY, color);
    }

    public static OptionalInt getOuterColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NbtKeys.OUTER_COLOR_NBT_KEY)) return OptionalInt.empty();
        return OptionalInt.of(nbt.getInt(NbtKeys.OUTER_COLOR_NBT_KEY));
    }

    public static void setOuterColor(ItemStack stack, int color) {
        stack.getOrCreateNbt().putInt(NbtKeys.OUTER_COLOR_NBT_KEY, color);
    }

    public static OptionalInt getStage(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NbtKeys.STAGE_NBT_KEY)) return OptionalInt.empty();
        return OptionalInt.of(nbt.getInt(NbtKeys.STAGE_NBT_KEY));
    }

    public static void setStage(ItemStack stack, int stage) {
        stack.getOrCreateNbt().putInt(NbtKeys.STAGE_NBT_KEY, MathHelper.clamp(stage, 0, NeMuelchProperties.MAX_CRYSTAL_STAGE));
    }

    @SuppressWarnings("unused")
    public static void clearData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        nbt.remove(NbtKeys.INNER_COLOR_NBT_KEY);
        nbt.remove(NbtKeys.OUTER_COLOR_NBT_KEY);
        nbt.remove(NbtKeys.STAGE_NBT_KEY);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user.isCreative() && user.isSneaking()) {
            int currentStage = getStage(stack).orElse(0);
            setStage(stack, currentStage + 1);
            user.setStackInHand(hand, stack);
            return TypedActionResult.success(stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (!world.isClient()) {
            if (world.getBlockEntity(pos) instanceof CrystalBlockEntity crystalBlockEntity) {
                getInnerColor(stack).ifPresent(crystalBlockEntity::setInnerColor);
                getOuterColor(stack).ifPresent(crystalBlockEntity::setOuterColor);
            }
        }
        return super.postPlacement(pos, world, player, stack, state);
    }

    @SuppressWarnings("unused")
    public enum ColorPresets {
        CRIMSON(0xff6699, 0xff3366);

        private final int innerColor;
        private final int outerColor;

        ColorPresets(int innerColor, int outerColor) {
            this.innerColor = innerColor;
            this.outerColor = outerColor;
        }

        public int getInnerColor() {
            return innerColor;
        }

        public int getOuterColor() {
            return outerColor;
        }
    }
}
