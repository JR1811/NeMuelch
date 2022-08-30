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

public class PestcaneStationRecipe implements Recipe<SimpleInventory> {

    private final Identifier id;
    private final ItemStack output;
    private final DefaultedList<Ingredient> recipeItems;


    public PestcaneStationRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems) {

        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {

        if (!world.isClient()) {

            return recipeItems.get(0).test(inventory.getStack(0));   // input for slot 0
        }

        return false;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {

        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return output.copy();
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

    public static class Serializer implements RecipeSerializer<PestcaneStationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "pestcane_recharging";


        @Override
        public PestcaneStationRecipe read(Identifier id, JsonObject json) {

            ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));

            JsonArray ingredients = JsonHelper.getArray(json, "ingredients");
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(1, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new PestcaneStationRecipe(id, output, inputs);
        }

        @Override
        public PestcaneStationRecipe read(Identifier id, PacketByteBuf buf) {

            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromPacket(buf));
            }

            ItemStack output = buf.readItemStack();
            return new PestcaneStationRecipe(id, output, inputs);
        }

        @Override
        public void write(PacketByteBuf buf, PestcaneStationRecipe recipe) {

            buf.writeInt(recipe.getIngredients().size());

            for (Ingredient ing : recipe.getIngredients()) {
                ing.write(buf);
            }
            buf.writeItemStack(recipe.getOutput());
        }
    }
}
