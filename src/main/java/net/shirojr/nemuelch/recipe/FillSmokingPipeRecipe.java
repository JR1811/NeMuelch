package net.shirojr.nemuelch.recipe;

import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.custom.supportItem.SmokingPipeItem;
import net.shirojr.nemuelch.util.HandInventory;

import java.util.List;

public class FillSmokingPipeRecipe extends AbstractHandCraftingRecipe {
    public FillSmokingPipeRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(HandInventory inventory, World world) {
        if (!(inventory.getMainHandStack().getItem() instanceof SmokingPipeItem smokingPipeItem)) return false;
        if (inventory.getOffHandStack().isEmpty()) return false;
        if (smokingPipeItem.isFull(inventory.getMainHandStack())) return false;
        List<StatusEffectInstance> effects = getEffects(inventory.getOffHandStack());
        return !effects.isEmpty();
    }

    @Override
    public ItemStack craft(HandInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack pipeStack = inventory.getMainHandStack().copy();
        if (!(pipeStack.getItem() instanceof SmokingPipeItem smokingPipeItem)) return ItemStack.EMPTY;
        if (!smokingPipeItem.setFilling(pipeStack, getEffects(inventory.getOffHandStack()))) {
            return ItemStack.EMPTY;
        }
        return pipeStack;
    }

    public List<StatusEffectInstance> getEffects(ItemStack stack) {
        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        if (stack.getItem() instanceof SuspiciousStewItem) {
            NbtCompound nbtCompound = stack.getNbt();
            if (nbtCompound != null && nbtCompound.contains("Effects", NbtElement.LIST_TYPE)) {
                NbtList nbtList = nbtCompound.getList("Effects", NbtElement.COMPOUND_TYPE);

                for (int i = 0; i < nbtList.size(); i++) {
                    NbtCompound nbtCompound2 = nbtList.getCompound(i);
                    int j;
                    if (nbtCompound2.contains("EffectDuration", NbtElement.NUMBER_TYPE)) {
                        j = nbtCompound2.getInt("EffectDuration");
                    } else {
                        j = 160;
                    }

                    StatusEffect statusEffect = StatusEffect.byRawId(nbtCompound2.getInt("EffectId"));
                    if (statusEffect != null) {
                        effects.add(new StatusEffectInstance(statusEffect, j));
                    }
                }
            }
        }
        return effects;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<FillSmokingPipeRecipe> {
        public static final FillSmokingPipeRecipe.Serializer INSTANCE = new FillSmokingPipeRecipe.Serializer();

        @Override
        public FillSmokingPipeRecipe read(Identifier id, JsonObject json) {
            return new FillSmokingPipeRecipe(id);
        }

        @Override
        public FillSmokingPipeRecipe read(Identifier id, PacketByteBuf buf) {
            return new FillSmokingPipeRecipe(id);
        }

        @Override
        public void write(PacketByteBuf buf, FillSmokingPipeRecipe recipe) {
        }
    }

    public static class Type implements RecipeType<FillSmokingPipeRecipe> {
        public static final FillSmokingPipeRecipe.Type INSTANCE = new FillSmokingPipeRecipe.Type();

        private Type() {
        }
    }
}
