package net.shirojr.nemuelch.compat.cca.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;

public enum BlightType implements StringIdentifiable {
    WITHERING(StatusEffects.WITHER),
    POISONOUS(StatusEffects.POISON),
    CORRUPTED(null),
    SPREADING(null);

    @SuppressWarnings("deprecation")
    public static final Codec<BlightType> CODEC = StringIdentifiable.createCodec(BlightType::values);
    public static final BlightType[] CACHED_VALUES = values();

    private final @Nullable StatusEffect craftingEffect;

    BlightType(@Nullable StatusEffect craftingEffect) {
        this.craftingEffect = craftingEffect;
    }

    public @Nullable StatusEffect getCraftingEffect() {
        return craftingEffect;
    }

    public static void applyToStack(ItemStack stack, Set<BlightType> types) {
        if (types.isEmpty()) return;
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.remove(NbtKeys.BLIGHT_TYPES);
        NbtList nbtList = new NbtList();
        for (BlightType type : types) {
            NbtString entry = NbtString.of(type.asString());
            nbtList.add(entry);
        }
        nbt.put(NbtKeys.BLIGHT_TYPES, nbtList);
    }

    public static EnumSet<BlightType> fromStack(ItemStack stack) {
        EnumSet<BlightType> result = EnumSet.noneOf(BlightType.class);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return result;
        NbtList nbtList = nbt.getList(NbtKeys.BLIGHT_TYPES, NbtElement.STRING_TYPE);
        for (int i = 0; i < nbtList.size(); i++) {
            String entry = nbtList.getString(i);
            result.add(BlightType.fromString(entry));
        }
        return result;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }

    public static BlightType fromString(String name) {
        for (BlightType cachedValue : CACHED_VALUES) {
            if (cachedValue.asString().equals(name)) return cachedValue;
        }
        throw new NoSuchElementException();
    }

    public static class ArgumentType extends EnumArgumentType<BlightType> {
        private ArgumentType() {
            super(BlightType.CODEC, BlightType::values);
        }

        public static ArgumentType blightType() {
            return new ArgumentType();
        }

        public static BlightType getBlockRotation(CommandContext<ServerCommandSource> context, String id) {
            return context.getArgument(id, BlightType.class);
        }
    }
}
