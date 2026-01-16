package io.github.hadron13.rubberworks.compat.kubejs;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import io.github.hadron13.rubberworks.compat.kubejs.schemas.ProcessingRecipeSchema;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;

import java.util.HashMap;
import java.util.Map;

public class KubeJSRubberworksPlugin extends KubeJSPlugin {

    private static final Map<RubberworksRecipeTypes, RecipeSchema> recipeSchemas = new HashMap<>();

    static {
        recipeSchemas.put(RubberworksRecipeTypes.SAPPING, ProcessingRecipeSchema.PROCESSING_WITH_TIME);
        recipeSchemas.put(RubberworksRecipeTypes.COMPRESSING, ProcessingRecipeSchema.PROCESSING_WITH_TIME);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (var createRecipeType : RubberworksRecipeTypes.values()) {
            if (createRecipeType.getSerializer() instanceof ProcessingRecipeSerializer<?>) {
                var schema = recipeSchemas.getOrDefault(createRecipeType, ProcessingRecipeSchema.PROCESSING_DEFAULT);
                event.register(createRecipeType.getId(), schema);
            }
        }
    }
}