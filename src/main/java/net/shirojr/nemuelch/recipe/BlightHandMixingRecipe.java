package net.shirojr.nemuelch.recipe;

import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.util.HandInventory;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class BlightHandMixingRecipe extends AbstractHandCraftingRecipe {
    private final Ingredient base;
    private final Ingredient poison;

    private final Predicate<ItemStack> isBlighted = stack -> {
        List<StatusEffectInstance> potionEffects = PotionUtil.getPotionEffects(stack);
        for (StatusEffectInstance entry : potionEffects) {
            for (BlightType blightType : BlightType.CACHED_VALUES) {
                if (blightType.getIngredients() == null) continue;
                if (blightType.getIngredients().equals(entry.getEffectType())) return true;
            }
        }
        return false;
    };

    public BlightHandMixingRecipe(Identifier id, Ingredient base, Ingredient poison) {
        super(id);
        this.base = base;
        this.poison = poison;
    }

    @Override
    public boolean matches(HandInventory inventory, World world) {
        if (!inventory.isFull()) return false;
        ItemStack mainHandStack = inventory.getMainHandStack();
        ItemStack offHandStack = inventory.getOffHandStack();
        return (base.test(mainHandStack) && poison.test(offHandStack) && isBlighted.test(offHandStack)) ||
                (poison.test(mainHandStack) && base.test(offHandStack) && isBlighted.test(mainHandStack));
    }

    @Override
    public ItemStack craft(HandInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack mainHandStack = inventory.getMainHandStack();
        ItemStack offHandStack = inventory.getOffHandStack();

        ItemStack baseStack = null;
        ItemStack modifierStack = null;

        if (base.test(mainHandStack) && poison.test(offHandStack) && isBlighted.test(offHandStack)) {
            baseStack = mainHandStack;
            modifierStack = offHandStack;
        } else if (base.test(offHandStack) && poison.test(mainHandStack) && isBlighted.test(mainHandStack)) {
            baseStack = offHandStack;
            modifierStack = mainHandStack;
        }
        if (baseStack == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = baseStack.copy();
        result.setCount(1);
        addResultNbt(modifierStack, result);
        return result;
    }

    private static void addResultNbt(ItemStack modifier, ItemStack result) {
        EnumSet<BlightType> types = EnumSet.noneOf(BlightType.class);
        for (StatusEffectInstance potionEffect : PotionUtil.getPotionEffects(modifier)) {
            for (BlightType entry : BlightType.CACHED_VALUES) {
                if (entry.getIngredients() == null) continue;
                if (!entry.getIngredients().equals(potionEffect.getEffectType())) continue;
                types.add(entry);
            }
        }
        BlightType.applyToStack(result, types);
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return null;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(HandInventory inventory) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getStacks().size(); i++) {
            ItemStack itemStack = inventory.getStacks().get(i);
            if (!itemStack.getRecipeRemainder().isEmpty()) {
                remainder.set(i, itemStack.getRecipeRemainder());
            } else if (itemStack.getItem() instanceof PotionItem) {
                remainder.set(i, new ItemStack(Items.GLASS_BOTTLE));
            } else if (itemStack.getItem() instanceof SplashPotionItem) {
                remainder.set(i, new ItemStack(Items.GLASS_BOTTLE));
            } else if (itemStack.getItem() instanceof LingeringPotionItem) {
                remainder.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return remainder;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<BlightHandMixingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public BlightHandMixingRecipe read(Identifier id, JsonObject json) {
            Ingredient base = Ingredient.fromJson(JsonHelper.getObject(json, "base"));
            Ingredient poison = Ingredient.fromJson(JsonHelper.getObject(json, "blight"));
            return new BlightHandMixingRecipe(id, base, poison);
        }

        @Override
        public BlightHandMixingRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient base = Ingredient.fromPacket(buf);
            Ingredient poison = Ingredient.fromPacket(buf);
            return new BlightHandMixingRecipe(id, base, poison);
        }

        @Override
        public void write(PacketByteBuf buf, BlightHandMixingRecipe recipe) {
            recipe.base.write(buf);
            recipe.poison.write(buf);
        }
    }

    public static class Type implements RecipeType<BlightHandMixingRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }
    }
}
