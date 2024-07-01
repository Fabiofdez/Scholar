package io.github.mortuusars.scholar.integration.jei.recipe;

import io.github.mortuusars.scholar.recipe.NbtTransferringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record NbtTransferringShapelessExtension() implements ICraftingCategoryExtension<NbtTransferringRecipe> {

    @Override
    public void setRecipe(@NotNull RecipeHolder<NbtTransferringRecipe> recipeHolder, @NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper craftingGridHelper, @NotNull IFocusGroup focuses) {
        NbtTransferringRecipe recipe = recipeHolder.value();
        List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> List.of(ingredient.getItems()))
                .collect(Collectors.toList());

        inputs.add(0, List.of(recipe.getTransferIngredient().getItems()));

        ItemStack resultItem = recipe.getResult();

        int width = getWidth(recipeHolder);
        int height = getHeight(recipeHolder);
        craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
        craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
    }
}