package net.shirojr.nemuelch.recipe;

import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.shirojr.nemuelch.util.HandInventory;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.List;
import java.util.function.Predicate;

public class PoisonHandMixingRecipe extends AbstractHandCraftingRecipe {
    private final Ingredient base;
    private final Ingredient poison;

    private final Predicate<ItemStack> isPoison = stack -> {
        List<StatusEffectInstance> potionEffects = PotionUtil.getPotionEffects(stack);
        for (StatusEffectInstance entry : potionEffects) {
            if (entry.getEffectType().equals(StatusEffects.POISON)) {
                return true;
            }
        }
        return false;
    };

    public PoisonHandMixingRecipe(Identifier id, Ingredient base, Ingredient poison) {
        super(id);
        this.base = base;
        this.poison = poison;
    }

    @Override
    public boolean matches(HandInventory inventory, World world) {
        if (!inventory.isFull()) return false;
        ItemStack mainHandStack = inventory.getMainHandStack();
        ItemStack offHandStack = inventory.getOffHandStack();
        return (base.test(mainHandStack) && poison.test(offHandStack) && isPoison.test(offHandStack)) ||
                (poison.test(mainHandStack) && base.test(offHandStack) && isPoison.test(mainHandStack));
    }

    @Override
    public ItemStack craft(HandInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack mainHandStack = inventory.getMainHandStack();
        ItemStack offHandStack = inventory.getOffHandStack();

        ItemStack baseStack = null;

        if (base.test(mainHandStack) && poison.test(offHandStack) && isPoison.test(offHandStack)) {
            baseStack = mainHandStack;
        } else if (base.test(offHandStack) && poison.test(mainHandStack) && isPoison.test(mainHandStack)) {
            baseStack = offHandStack;
        }
        if (baseStack == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = baseStack.copy();
        result.setCount(1);
        addResultNbt(result);
        return result;
    }

    private static void addResultNbt(ItemStack result) {
        result.getOrCreateNbt().putBoolean(NbtKeys.POISONED, true);
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

    public static class Serializer implements RecipeSerializer<PoisonHandMixingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public PoisonHandMixingRecipe read(Identifier id, JsonObject json) {
            Ingredient base = Ingredient.fromJson(JsonHelper.getObject(json, "base"));
            Ingredient poison = Ingredient.fromJson(JsonHelper.getObject(json, "poison"));
            return new PoisonHandMixingRecipe(id, base, poison);
        }

        @Override
        public PoisonHandMixingRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient base = Ingredient.fromPacket(buf);
            Ingredient poison = Ingredient.fromPacket(buf);
            return new PoisonHandMixingRecipe(id, base, poison);
        }

        @Override
        public void write(PacketByteBuf buf, PoisonHandMixingRecipe recipe) {
            recipe.base.write(buf);
            recipe.poison.write(buf);
        }
    }

    public static class Type implements RecipeType<PoisonHandMixingRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }
    }
}
