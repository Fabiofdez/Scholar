package io.github.mortuusars.scholar.recipe;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.mortuusars.scholar.Scholar;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NbtTransferringRecipe extends CustomRecipe {
    private final ItemStack result;
    private final Ingredient transferIngredient;
    private final NonNullList<Ingredient> ingredients;

    public final Codec<NbtTransferringRecipe> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<NbtTransferringRecipe, T>> decode(DynamicOps<T> ops, T input) {
            // TODO
            return null;
        }

        @Override
        public <T> DataResult<T> encode(NbtTransferringRecipe input, DynamicOps<T> ops, T prefix) {
            // TODO
            return null;
        }
    };

    public NbtTransferringRecipe(Ingredient transferIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(CraftingBookCategory.MISC);
        this.transferIngredient = transferIngredient;
        this.ingredients = ingredients;
        this.result = result;
    }

    public @NotNull Ingredient getTransferIngredient() {
        return transferIngredient;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Scholar.RecipeSerializers.NBT_TRANSFERRING.get();
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return getResult();
    }

    public @NotNull ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (getTransferIngredient().isEmpty() || ingredients.isEmpty())
            return false;

        List<Ingredient> unmatchedIngredients = new ArrayList<>(ingredients);
        unmatchedIngredients.add(0, getTransferIngredient());

        int itemsInCraftingGrid = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty())
                itemsInCraftingGrid++;

            if (itemsInCraftingGrid > ingredients.size() + 1)
                return false;

            if (!unmatchedIngredients.isEmpty()) {
                for (int j = 0; j < unmatchedIngredients.size(); j++) {
                    if (unmatchedIngredients.get(j).test(stack)) {
                        unmatchedIngredients.remove(j);
                        break;
                    }
                }
            }
        }

        return unmatchedIngredients.isEmpty() && itemsInCraftingGrid == ingredients.size() + 1;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        for (int index = 0; index < container.getContainerSize(); index++) {
            ItemStack itemStack = container.getItem(index);

            if (getTransferIngredient().test(itemStack)) {
                return transferNbt(itemStack, getResultItem(registryAccess).copy());
            }
        }

        return getResultItem(registryAccess);
    }

    public @NotNull ItemStack transferNbt(ItemStack transferIngredientStack, ItemStack recipeResultStack) {
        @Nullable CompoundTag transferTag = transferIngredientStack.getTag();
        if (transferTag != null) {
            if (recipeResultStack.getTag() != null)
                recipeResultStack.getTag().merge(transferTag);
            else
                recipeResultStack.setTag(transferTag.copy());
        }
        return recipeResultStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return ingredients.size() <= width * height;
    }

    public static class Serializer implements RecipeSerializer<NbtTransferringRecipe> {
        @Override
        public Codec<NbtTransferringRecipe> codec() {
            return null;
//            return RecordCodecBuilder.create(new Function<RecordCodecBuilder.Instance<NbtTransferringRecipe>, App<RecordCodecBuilder.Mu<NbtTransferringRecipe>, NbtTransferringRecipe>>() {
//                @Override
//                public App<RecordCodecBuilder.Mu<NbtTransferringRecipe>, NbtTransferringRecipe> apply(RecordCodecBuilder.Instance<NbtTransferringRecipe> nbtTransferringRecipeInstance) {
//
//                    return null;
//                }
//            })
        }

        @Override
        public @NotNull NbtTransferringRecipe fromNetwork(FriendlyByteBuf buffer) {
            Ingredient transferredIngredient = Ingredient.fromNetwork(buffer);
            int ingredientsCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();

            return new NbtTransferringRecipe( transferredIngredient, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NbtTransferringRecipe recipe) {
            recipe.getTransferIngredient().toNetwork(buffer);
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResult());
        }

        private NonNullList<Ingredient> getIngredients(FriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = NonNullList.create();

            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            if (!ingredient.isEmpty())
                ingredients.add(ingredient);

            if (ingredients.isEmpty())
                throw new JsonParseException("No ingredients for a recipe.");
            else if (ingredients.size() > 3 * 3)
                throw new JsonParseException("Too many ingredients for a recipe. The maximum is 9.");
            return ingredients;
        }
    }
}
