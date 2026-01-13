package io.github.hadron13.rubberworks.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import io.github.hadron13.rubberworks.compat.kubejs.schemas.ProcessingRecipeSchema;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;

import java.util.HashMap;
import java.util.Map;

public class KubeJSRubberworksPlugin implements KubeJSPlugin {

    private static final Map<RubberworksRecipeTypes, RecipeSchema> recipeSchemas = new HashMap<>();

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(ProcessingRecipeSchema.SAPPING_FACTORY);
        registry.register(ProcessingRecipeSchema.COMPRESSING_FACTORY);

    }
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(RubberworksRecipeTypes.SAPPING.id, ProcessingRecipeSchema.SAPPING_SCHEMA);
        registry.register(RubberworksRecipeTypes.COMPRESSING.id, ProcessingRecipeSchema.COMPRESSING_SCHEMA);

    }
}