package net.shirojr.nemuelch.compat.cca.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum BlightType implements StringIdentifiable {
    WITHERING(StatusEffects.WITHER, new BlightIngredients(StatusEffects.WITHER), 7561558, () -> new BlightAction() {
        @Override
        public void onBlockBroken(ServerWorld world, long blightAge, BlockPos pos, PlayerEntity player) {
            BlightAction.super.onBlockBroken(world, blightAge, pos, player);
        }

        @Override
        public void onPickedUp(LivingEntity entity, ItemEntity stack, BlightType type) {
            BlightAction.super.onPickedUp(entity, stack, type);
            if (!(entity.getWorld() instanceof ServerWorld)) return;
        }
    }),
    POISONOUS(StatusEffects.POISON, new BlightIngredients(StatusEffects.POISON, Ingredient.ofItems(Items.POISONOUS_POTATO)), 8889187, () -> new BlightAction() {
        @Override
        public void onBlockBroken(ServerWorld world, long blightAge, @Nullable BlockPos pos, PlayerEntity player) {
            BlightAction.super.onBlockBroken(world, blightAge, pos, player);
        }
    }),
    CORRUPTED(null, new BlightIngredients(), 0x000000, () -> new BlightAction() {
        @Override
        public void onBlockBroken(ServerWorld world, long blightAge, @Nullable BlockPos pos, PlayerEntity player) {
            BlightAction.super.onBlockBroken(world, blightAge, pos, player);
        }
    }),
    AIRBORNE(null, new BlightIngredients(StatusEffects.LEVITATION, Ingredient.ofItems(Items.FEATHER)), 13565951, () -> new BlightAction() {
        @Override
        public void onBlockBroken(ServerWorld world, long blightAge, @Nullable BlockPos pos, PlayerEntity player) {
            BlightAction.super.onBlockBroken(world, blightAge, pos, player);
        }
    }),
    SPREADING(null, new BlightIngredients(), 16262179, () -> new BlightAction() {
        @Override
        public void onBlockBroken(ServerWorld world, long blightAge, @Nullable BlockPos pos, PlayerEntity player) {
            BlightAction.super.onBlockBroken(world, blightAge, pos, player);
        }
    });

    @SuppressWarnings("deprecation")
    public static final Codec<BlightType> CODEC = StringIdentifiable.createCodec(BlightType::values);
    public static final BlightType[] CACHED_VALUES = values();
    private static final Map<String, BlightType> BY_NAME = Arrays.stream(CACHED_VALUES)
            .collect(Collectors.toMap(BlightType::asString, Function.identity()));

    private final @Nullable StatusEffect effect;
    private final BlightIngredients ingredients;
    private final int debugColor;
    private final Supplier<BlightAction> actions;

    BlightType(@Nullable StatusEffect effect, BlightIngredients ingredients, int debugColor, Supplier<BlightAction> actions) {
        this.effect = effect;
        this.ingredients = ingredients;
        this.debugColor = debugColor;
        this.actions = actions;
    }

    public @Nullable StatusEffect getEffect() {
        return effect;
    }

    public BlightIngredients getIngredients() {
        return ingredients;
    }

    public int getDebugColor() {
        return debugColor;
    }

    public Supplier<BlightAction> getActions() {
        return actions;
    }

    /**
     * Applies Blight to ItemStack.
     * @param stack owner of NBT data
     * @param types leave empty to clear data
     */
    public static void applyToStack(ItemStack stack, Set<BlightType> types) {
        if (types.isEmpty()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;
            nbt.remove(NbtKeys.BLIGHT_TYPES);
            return;
        }
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

    public static boolean hasNoStackBlight(ItemStack stack) {
        return stack.getNbt() == null || !stack.getNbt().contains(NbtKeys.BLIGHT_TYPES);
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

    public static List<BlightType> asOrderedList(Set<BlightType> types) {
        List<BlightType> result = new ArrayList<>();
        for (BlightType type : CACHED_VALUES) {
            if (types.contains(type)) result.add(type);
        }
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
