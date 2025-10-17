package net.shirojr.nemuelch.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.custom.supportItem.BookWrapperItem;
import net.shirojr.nemuelch.mixin.access.ShapedRecipeAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookWrapperRecipe extends ShapedRecipe {
    private final BookWrapperItem.Part part;

    public BookWrapperRecipe(Identifier id, BookWrapperItem.Part part,
                             int width, int height, DefaultedList<Ingredient> input, ItemStack output) {
        super(id, "", CraftingRecipeCategory.MISC, width, height, input, output);
        this.part = part;
    }

    public BookWrapperItem.Part getPart() {
        return part;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        ItemStack baseStack = ItemStack.EMPTY;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (inventoryStack.isEmpty()) continue;
            if (inventoryStack.getItem() instanceof BookWrapperItem) {
                if (!baseStack.isEmpty()) return false;
                baseStack = inventoryStack;
            }
        }

        for (int x = 0; x <= inventory.getWidth() - this.getWidth(); x++) {
            for (int y = 0; y <= inventory.getHeight() - this.getHeight(); y++) {
                if (this.matchesPattern(inventory, x, y, true)) {
                    return true;
                }

                if (this.matchesPattern(inventory, x, y, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesPattern(RecipeInputInventory inv, int offsetX, int offsetY, boolean flipped) {
        for (int x = 0; x < inv.getWidth(); x++) {
            for (int y = 0; y < inv.getHeight(); y++) {
                int entryX = x - offsetX;
                int entryY = y - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (entryX >= 0 && entryY >= 0 && entryX < this.getWidth() && entryY < this.getHeight()) {
                    if (flipped) {
                        ingredient = this.getIngredients().get(this.getWidth() - entryX - 1 + entryY * this.getWidth());
                    } else {
                        ingredient = this.getIngredients().get(entryX + entryY * this.getWidth());
                    }
                }

                ItemStack stack = inv.getStack(x + y * inv.getWidth());
                if (ingredient.isEmpty() && stack.getItem() instanceof DyeItem) {
                    continue;
                }
                if (!ingredient.test(stack)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager dynamicRegistryManager) {
        ItemStack bookWrapperStack = ItemStack.EMPTY;
        List<DyeItem> dyeItems = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (inventoryStack.isEmpty()) continue;
            if (inventoryStack.getItem() instanceof BookWrapperItem) {
                if (!bookWrapperStack.isEmpty()) return ItemStack.EMPTY;
                bookWrapperStack = inventoryStack.copy();
                continue;
            }
            if (inventoryStack.getItem() instanceof DyeItem item) {
                dyeItems.add(item);
            }
        }
        ItemStack result = getOutput(dynamicRegistryManager).copy();
        if (!bookWrapperStack.isEmpty() && result.getItem().equals(bookWrapperStack.getItem())) {
            result = bookWrapperStack.copyWithCount(1);
        }
        this.getPart().setColor(result, this.getPart().getBlendedColor(result, dyeItems));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<BookWrapperRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public BookWrapperRecipe read(Identifier id, JsonObject json) {
            BookWrapperItem.Part part = BookWrapperItem.Part.fromString(JsonHelper.getString(json, "part"));

            Map<String, Ingredient> symbols = ShapedRecipeAccess.readSymbols(JsonHelper.getObject(json, "key"));
            String[] lines = ShapedRecipeAccess.removePadding(ShapedRecipeAccess.getPattern(JsonHelper.getArray(json, "pattern")));
            int width = lines[0].length();
            int height = lines.length;
            DefaultedList<Ingredient> ingredients = ShapedRecipeAccess.createPatternMatrix(lines, symbols, width, height);

            ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

            return new BookWrapperRecipe(id, part, width, height, ingredients, result);
        }

        @Override
        public BookWrapperRecipe read(Identifier id, PacketByteBuf buf) {
            BookWrapperItem.Part part = BookWrapperItem.Part.values()[buf.readVarInt()];
            int width = buf.readVarInt();
            int height = buf.readVarInt();
            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromPacket(buf));
            ItemStack result = buf.readItemStack();
            return new BookWrapperRecipe(id, part, width, height, ingredients, result);
        }

        @Override
        public void write(PacketByteBuf buf, BookWrapperRecipe recipe) {
            buf.writeVarInt(recipe.part.ordinal());
            buf.writeVarInt(recipe.getWidth());
            buf.writeVarInt(recipe.getHeight());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buf);
            }
            buf.writeItemStack(((ShapedRecipeAccess) recipe).getOutputStack());
        }
    }
}
