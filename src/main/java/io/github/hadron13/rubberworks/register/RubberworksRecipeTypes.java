package io.github.hadron13.rubberworks.register;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.blocks.compressor.CompressingRecipe;
import io.github.hadron13.rubberworks.blocks.compressor.CompressorBlockEntity;
import io.github.hadron13.rubberworks.blocks.sapper.SappingRecipe;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum RubberworksRecipeTypes implements IRecipeTypeInfo, StringRepresentable {
    SAPPING(SappingRecipe::new),
    COMPRESSING(CompressingRecipe::new);


    public final ResourceLocation id;
    public final Supplier<RecipeSerializer<?>> serializerSupplier;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
    @Nullable
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;


    RubberworksRecipeTypes(StandardProcessingRecipe.Factory<?> processingFactory) {
        this(() -> new StandardProcessingRecipe.Serializer<>(processingFactory));
    }

    RubberworksRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        id = Rubberworks.asResource(name);
        this.serializerSupplier = serializerSupplier;
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
        type = typeObject;
    }

    public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
        String stringId = id.toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return stringId;
            }
        };
    }



    public static void register(IEventBus modEventBus) {
        ShapedRecipePattern.setCraftingSize(9, 9);
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER.register(modEventBus);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }


    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I inv, Level world) {
        return world.getRecipeManager()
                .getRecipeFor(getType(), inv, world);
    }

    public Optional<CompressingRecipe> find(CompressorBlockEntity blockEntity, Level world) {
        if(world.isClientSide())
            return Optional.empty();
        List<RecipeHolder<CompressingRecipe>> allRecipes = world.getRecipeManager().getAllRecipesFor(RubberworksRecipeTypes.COMPRESSING.getType());

        Stream<CompressingRecipe> matchingRecipes =
                allRecipes.stream().filter(recipe -> CompressingRecipe.match(blockEntity, recipe.value()) ).map(RecipeHolder::value);

        return matchingRecipes.findAny();
    }


    @Override
    public String getSerializedName() {
        return id.toString();
    }

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Rubberworks.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Rubberworks.MODID);
    }

}