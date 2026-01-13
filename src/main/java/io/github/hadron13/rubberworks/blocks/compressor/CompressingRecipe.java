package io.github.hadron13.rubberworks.blocks.compressor;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.*;
import io.github.hadron13.rubberworks.register.RubberworksRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED;
import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SMOULDERING;
import static com.simibubi.create.content.processing.recipe.HeatCondition.HEATED;
import static com.simibubi.create.content.processing.recipe.HeatCondition.NONE;

public class CompressingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    public CompressingRecipe(ProcessingRecipeParams params) {
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

        IFluidHandler fluidCapability = blockEntity.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), null);

        if (fluidCapability == null)
            return false;

        SizedFluidIngredient fluidIngredient = recipe.getFluidIngredients().get(0);

        FluidStack available = fluidCapability.getFluidInTank(0);

        boolean ingredientMatch = fluidIngredient.test(available);
        boolean enoughFluid = available.getAmount() >= fluidIngredient.amount();

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

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SingleRecipeInput singleRecipeInput, Level level) {
        return false;
    }
}
