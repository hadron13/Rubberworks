package io.github.hadron13.rubberworks.blocks.sapper;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SappingRecipe extends StandardProcessingRecipe<RecipeInput> {

    public SappingRecipe(ProcessingRecipeParams params) {
        super(RubberworksRecipeTypes.SAPPING, params);

        Item logItem = this.getIngredients().get(0).getItems()[0].getItem();
        Item leafItem = this.getIngredients().get(1).getItems()[0].getItem();

        if(logItem instanceof BlockItem && leafItem instanceof BlockItem && !this.getFluidResults().isEmpty()){
            FluidStack result = this.getFluidResults().get(0);

            SapperBlockEntity.TreeType.registerTree(((BlockItem) logItem).getBlock(), ((BlockItem) leafItem).getBlock(), result);
        }else{
            Rubberworks.LOGGER.warn("Sapping recipe id: "+ this.params.toString() +" contains non-block ingredients");
        }

    }

    @Override
    protected boolean canSpecifyDuration() {
        return false;
    }
    @Override
    protected int getMaxInputCount() {
        return 2;
    }
    @Override
    protected int getMaxOutputCount(){return 0;}
    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return false;
    }
}