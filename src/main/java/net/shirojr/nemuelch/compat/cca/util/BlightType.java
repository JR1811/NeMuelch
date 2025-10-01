package net.shirojr.nemuelch.compat.cca.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BlightType implements StringIdentifiable {
    WITHERING(new BlightIngredients(StatusEffects.WITHER)),
    POISONOUS(new BlightIngredients(StatusEffects.POISON, Ingredient.ofItems(Items.POISONOUS_POTATO))),
    CORRUPTED(new BlightIngredients()), // farmland progress regresses
    AIRBORNE(new BlightIngredients(StatusEffects.LEVITATION)),
    SPREADING(new BlightIngredients());

    @SuppressWarnings("deprecation")
    public static final Codec<BlightType> CODEC = StringIdentifiable.createCodec(BlightType::values);
    public static final BlightType[] CACHED_VALUES = values();
    private static final Map<String, BlightType> BY_NAME = Arrays.stream(CACHED_VALUES)
            .collect(Collectors.toMap(BlightType::asString, Function.identity()));

    private final BlightIngredients ingredients;

    BlightType(BlightIngredients ingredients) {
        this.ingredients = ingredients;
    }

    public BlightIngredients getIngredients() {
        return ingredients;
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
            BlightType type = fromStringOrNull(entry);
            if (type == null) continue;
            result.add(type);
        }
        return result;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }

    public static BlightType fromString(String name) {
        BlightType result = fromStringOrNull(name);
        if (result == null) {
            throw new NoSuchElementException("Unknown BlightType: " + name);
        }
        return result;
    }

    public static BlightType fromStringOrNull(String name) {
        return BY_NAME.get(name);
    }

    public static EnumSet<BlightType> typesToEnumSet(BlightType... types) {
        if (types.length == 0) return EnumSet.noneOf(BlightType.class);
        EnumSet<BlightType> result = EnumSet.of(types[0]);
        result.addAll(Arrays.asList(types).subList(1, types.length));
        return result;
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
