package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.init.NeMuelchItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

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

    @SuppressWarnings("Convert2MethodRef")
    public enum Type {
        BIG(() -> NeMuelchItems.MIASMA_BIG.getDefaultStack()),
        MEDIUM(() -> NeMuelchItems.MIASMA_MEDIUM.getDefaultStack()),
        SMALL(() -> NeMuelchItems.MIASMA_SMALL.getDefaultStack());

        private final Supplier<ItemStack> defaultStack;

        Type(Supplier<ItemStack> defaultStack) {
            this.defaultStack = defaultStack;
        }

        public ItemStack getDefaultStack() {
            return defaultStack.get();
        }
    }

    public enum ColorPreset {
        RED(2164227, null),
        BROWN(1839878, null),
        GREEN(1652235, null),
        BLUE(1118241, null);

        @Nullable
        private final Integer innerColor;
        @Nullable
        private final Integer outerColor;

        ColorPreset(@Nullable Integer innerColor, @Nullable Integer outerColor) {
            this.innerColor = innerColor;
            this.outerColor = outerColor;
        }

        public @Nullable Integer getInnerColor() {
            return innerColor;
        }

        public @Nullable Integer getOuterColor() {
            return outerColor;
        }

        public ItemStack getColoredStack(Type type) {
            ItemStack stack = type.getDefaultStack();
            MiasmaItem.setColor(stack, Part.INNER, Optional.ofNullable(getInnerColor()).orElse(Part.INNER.getDefaultColor()));
            MiasmaItem.setColor(stack, Part.OUTER, Optional.ofNullable(getOuterColor()).orElse(Part.OUTER.getDefaultColor()));
            return stack;
        }
    }
}
