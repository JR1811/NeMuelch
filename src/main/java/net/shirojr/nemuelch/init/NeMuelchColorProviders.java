package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.shirojr.nemuelch.block.custom.CrystalBlock;
import net.shirojr.nemuelch.block.entity.custom.CrystalBlockEntity;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CrystalBlockItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.item.custom.supportItem.BookWrapperItem;
import net.shirojr.nemuelch.item.custom.supportItem.SmokingPipeItem;
import net.shirojr.nemuelch.util.helper.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class NeMuelchColorProviders {
    static {
        ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::getBookWrapperColor, NeMuelchItems.BOOK_WRAPPER);
        for (MiasmaItem miasmaItem : NeMuelchItems.MIASMA_ITEMS) {
            ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::getMiasmaColor, miasmaItem);
        }
        for (SmokingPipeItem smokingPipe : NeMuelchItems.SMOKING_PIPES) {
            ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::getSmokingPipeColor, smokingPipe);
        }

        for (CrystalBlock crystal : NeMuelchBlocks.CRYSTALS) {
            ColorProviderRegistry.BLOCK.register(NeMuelchColorProviders::getCrystalBlockColor, crystal);
        }

        for (CrystalBlockItem crystal : NeMuelchItems.CRYSTALS) {
            ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::getCrystalItemColor, crystal);
        }
    }

    private static int getCrystalItemColor(ItemStack stack, int tintIndex) {
        if (!(stack.getItem() instanceof CrystalBlockItem)) return 0;
        if (tintIndex == 0) return CrystalBlockItem.getInnerColor(stack).orElse(0);
        if (tintIndex == 1) return CrystalBlockItem.getOuterColor(stack).orElse(0);
        return 0;
    }

    private static int getCrystalBlockColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
        if (state == null || world == null || pos == null) return 0;
        if (!(world.getBlockEntity(pos) instanceof CrystalBlockEntity crystalBlockEntity)) return 0;
        if (tintIndex == 0) return crystalBlockEntity.getInnerColor();
        if (tintIndex == 1) return crystalBlockEntity.getOuterColor();
        return 0;
    }

    private static int getSmokingPipeColor(ItemStack stack, int index) {
        if (!(stack.getItem() instanceof SmokingPipeItem smokingPipeItem)) return 0;
        List<StatusEffectInstance> filling = smokingPipeItem.getFilling(stack);
        if (filling.isEmpty()) return 0;
        List<Vector3f> colorMix = new ArrayList<>();
        for (StatusEffectInstance statusEffectInstance : filling) {
            colorMix.add(ColorHelper.getColorFromDec(statusEffectInstance.getEffectType().getColor()));
        }
        return ColorHelper.getColorFromVec(ColorHelper.mixColorsAverage(colorMix));
    }

    private static int getBookWrapperColor(ItemStack stack, int index) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0;
        Integer wrapperColor = BookWrapperItem.Part.WRAPPER.getColor(stack);
        Integer stripColor = BookWrapperItem.Part.STRIP.getColor(stack);
        Integer sigilColor = BookWrapperItem.Part.SIGIL.getColor(stack);

        if (index == 0 && wrapperColor != null) {
            return wrapperColor;
        }
        if (index == 1 && stripColor != null) {
            return stripColor;
        }
        if (index == 2 && sigilColor != null) {
            return sigilColor;
        }
        return 0;
    }

    public static int getMiasmaColor(ItemStack stack, int index) {
        return MiasmaItem.getColor(stack, MiasmaItem.Part.values()[index]);
    }

    public static void initialize() {
        // static initialisation
    }
}
