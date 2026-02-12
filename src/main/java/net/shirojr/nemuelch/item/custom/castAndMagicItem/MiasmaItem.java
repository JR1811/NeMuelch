package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class MiasmaItem extends Item {
    private final Type type;

    public MiasmaItem(Settings settings, Type type) {
        super(settings);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static void setColor(ItemStack stack, Part part, int color) {
        stack.getOrCreateNbt().putInt(part.getColorNbtKey(), color);
    }

    public static int getColor(ItemStack stack, Part part) {
        NbtCompound nbt = stack.getNbt();
        String colorNbtKey = part.getColorNbtKey();
        if (nbt == null || !nbt.contains(colorNbtKey)) return part.getDefaultColor();
        return Optional.of(nbt.getInt(colorNbtKey)).orElse(part.getDefaultColor());
    }

    public enum Part {
        INNER("InnerColor", 920087),
        OUTER("OuterColor", 16777215);

        private final String colorNbtKey;
        private final int defaultColor;

        Part(String colorNbtKey, int defaultColor) {
            this.colorNbtKey = colorNbtKey;
            this.defaultColor = defaultColor;
        }

        public String getColorNbtKey() {
            return colorNbtKey;
        }

        public int getDefaultColor() {
            return defaultColor;
        }
    }

    public enum Type {
        BIG,
        MEDIUM,
        SMALL
    }
}
