package io.github.hadron13.rubberworks.blocks.compressor;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED;
import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SMOULDERING;
import static com.simibubi.create.content.processing.recipe.HeatCondition.HEATED;
import static com.simibubi.create.content.processing.recipe.HeatCondition.NONE;

public class CompressingRecipe extends ProcessingRecipe<RecipeWrapper> {
    public CompressingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(RubberworksRecipeTypes.COMPRESSING, params);
    }
    public static boolean match(CompressorBlockEntity blockEntity, CompressingRecipe recipe) {
        if(recipe == null || blockEntity == null || blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide)
            return false;

        HeatCondition heatRequirement = recipe.getRequiredHeat();

        BlazeBurnerBlock.HeatLevel heatProvided = blockEntity.getHeat();

        if(heatRequirement == HEATED && !heatProvided.isAtLeast(KINDLED))
            return false;

        if(heatRequirement == NONE && heatProvided != SMOULDERING)
            return false;

        IFluidHandler fluidCapability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (fluidCapability == null)
            return false;

        FluidIngredient fluidIngredient = recipe.getFluidIngredients().get(0);

        FluidStack available = fluidCapability.getFluidInTank(0);

        boolean ingredientMatch = fluidIngredient.test(available);
        boolean enoughFluid = available.getAmount() >= fluidIngredient.getRequiredAmount();

        return ingredientMatch && enoughFluid;
    }

    public boolean sameInOutAs(CompressingRecipe otherRecipe, RegistryAccess registryAccess) {
        if(otherRecipe == null)
            return false;

        if(!this.getFluidIngredients().equals(otherRecipe.fluidIngredients))
            return false;

        if(!this.getResultItem(registryAccess).equals(otherRecipe.getResultItem(registryAccess)))
            return false;

        return true;
    }

    @Override
    public boolean matches(RecipeWrapper inv, @Nonnull Level worldIn) {
        return false;
    }
    @Override
    public int getMaxInputCount(){
        return 0;
    }
    @Override
    public int getMaxOutputCount(){
        return 1;
    }
    @Override
    public int getMaxFluidInputCount(){
        return 1;
    }

    @Override
    public boolean canRequireHeat() {return true;}


}
