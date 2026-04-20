package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.item.custom.supportItem.BookWrapperItem;
import net.shirojr.nemuelch.item.custom.supportItem.SmokingPipeItem;
import net.shirojr.nemuelch.util.helper.ColorHelper;
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
            ColorProviderRegistry.ITEM.register(NeMuelchColorProviders::getSmokingPipeColor);
        }
    }

    private static int getSmokingPipeColor(ItemStack stack, int index) {
        List<StatusEffectInstance> filling = SmokingPipeItem.getFilling(stack);
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
