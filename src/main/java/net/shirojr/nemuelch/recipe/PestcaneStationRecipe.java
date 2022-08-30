package net.shirojr.nemuelch.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class PestcaneStationRecipe implements Recipe<SimpleInventory> {

    private final Identifier id;
    private final ItemStack output;

    private final Ingredient energy;
    private final Ingredient cane;
    //private final DefaultedList<Ingredient> recipeItems;


    public PestcaneStationRecipe(Identifier id, ItemStack output, Ingredient energy, Ingredient cane) {

        this.id = id;
        this.output = output;

        this.energy = energy;
        this.cane = cane;
        //this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {

        if (!world.isClient()) {

            return this.energy.test(inventory.getStack(0)) && this.cane.test(inventory.getStack(1));
        }

        return false;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {

        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getOutput() {
        return this.output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<PestcaneStationRecipe> {
        private Type() {
        }

        public static final Type INSTANCE = new Type();
        public static final String ID = "pestcane_recharging";
    }

    public boolean isEmpty() {
        return Stream.of(this.energy, this.cane).anyMatch((ingredient) -> {
            return ingredient.getMatchingStacks().length == 0;
        });
    }

    public static class Serializer implements RecipeSerializer<PestcaneStationRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "pestcane_recharging";

        @Override
        public PestcaneStationRecipe read(Identifier id, JsonObject json) {

            ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));

            Ingredient energy = Ingredient.fromJson(JsonHelper.getObject(json, "energy"));
            Ingredient cane = Ingredient.fromJson(JsonHelper.getObject(json, "cane"));

            return new PestcaneStationRecipe(id, output, energy, cane);
        }

        @Override
        public PestcaneStationRecipe read(Identifier id, PacketByteBuf buf) {

            ItemStack output = buf.readItemStack();

            Ingredient energy = Ingredient.fromPacket(buf);
            Ingredient cane = Ingredient.fromPacket(buf);

            return new PestcaneStationRecipe(id, output, energy, cane);
        }

        @Override
        public void write(PacketByteBuf buf, PestcaneStationRecipe recipe) {

            recipe.energy.write(buf);
            recipe.cane.write(buf);

            buf.writeItemStack(recipe.output);
        }
    }
}
