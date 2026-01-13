package io.github.hadron13.rubberworks.compat.jei;

import com.simibubi.create.AllBlocks;
import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.blocks.compressor.CompressingRecipe;
import io.github.hadron13.rubberworks.blocks.sapper.SappingRecipe;
import io.github.hadron13.rubberworks.compat.jei.category.*;
import io.github.hadron13.rubberworks.register.RubberworksBlocks;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import com.simibubi.create.compat.jei.*;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static mezz.jei.api.recipe.RecipeType.createRecipeHolderType;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class RubberworksJEI implements IModPlugin {

    private static final ResourceLocation ID = Rubberworks.asResource("jei_plugin");

    public IIngredientManager ingredientManager;
    private final List<CreateRecipeCategory<?>> modCategories = new ArrayList<>();


    private void loadCategories() {

        this.modCategories.clear();

        CreateRecipeCategory<?>
                sapping = builder(SappingRecipe.class)
                .addTypedRecipes(RubberworksRecipeTypes.SAPPING)
                .catalyst(RubberworksBlocks.SAPPER::get)
                .itemIcon(RubberworksBlocks.SAPPER.get())
                .emptyBackground(177, 103)
                .build("sapping", SappingCategory::new);

        CreateRecipeCategory<?>
                compressing = builder(CompressingRecipe.class)
                .addTypedRecipes(RubberworksRecipeTypes.COMPRESSING)
                .catalyst(RubberworksBlocks.COMPRESSOR::get)
                .catalyst(AllBlocks.BLAZE_BURNER::get)
                .itemIcon(RubberworksBlocks.COMPRESSOR.get())
                .emptyBackground(177,75)
                .build("compressing", CompressingCategory::new);

    }


    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(modCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();

        modCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        modCategories.forEach(c -> c.registerCatalysts(registration));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new BlueprintTransferHandler(), RecipeTypes.CRAFTING);
    }


    private class CategoryBuilder<T extends Recipe<?>> {
        private final Class<? extends T> recipeClass;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<RecipeHolder<T>>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<RecipeHolder<T>>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public <I extends RecipeInput, R extends Recipe<I>> CategoryBuilder<T> addTypedRecipes(Supplier<net.minecraft.world.item.crafting.RecipeType<R>> recipeType) {
            return addRecipeListConsumer(recipes -> RubberworksJEI.<T>consumeTypedRecipes(recipe -> {
                if (recipeClass.isInstance(recipe.value()))
                    //noinspection unchecked - checked by if statement above
                    recipes.add((RecipeHolder<T>) recipe);
            }, recipeType.get()));
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CreateRecipeCategory<T> build(String id, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<RecipeHolder<T>>> recipesSupplier;
            recipesSupplier = () -> {
                List<RecipeHolder<T>> recipes = new ArrayList<>();
                for (Consumer<List<RecipeHolder<T>>> consumer : recipeListConsumers) {consumer.accept(recipes);}
                return recipes;
            };
            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                    createRecipeHolderType(Rubberworks.asResource(id)),
                    Component.translatable( "rubberworks.recipe." + id),
                    background,
                    icon,
                    recipesSupplier,
                    catalysts
            );

            CreateRecipeCategory<T> category = factory.create(info);
            modCategories.add(category);
            return category;
        }

    }

    public static void consumeAllRecipes(Consumer<? super RecipeHolder<?>> consumer) {
        Minecraft.getInstance()
                .getConnection()
                .getRecipeManager()
                .getRecipes()
                .forEach(consumer);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<RecipeHolder<?>> consumer, RecipeType<?> type) {
        List<? extends RecipeHolder<?>> map = Minecraft.getInstance()
                .getConnection()
                .getRecipeManager().getAllRecipesFor((RecipeType) type);
        if (!map.isEmpty())
            map.forEach(consumer);
    }




}
