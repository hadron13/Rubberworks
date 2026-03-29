package io.github.hadron13.rubberworks.items;

import com.simibubi.create.AllSoundEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class PlungerItem extends Item {
    public PlungerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();

        IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, context.getClickedPos(), null);
        if(cap == null)
            return InteractionResult.FAIL;

        for(int i = 0; i < cap.getTanks(); i++){
            FluidStack fluid = cap.getFluidInTank(i);
            if(fluid.isEmpty())
                continue;

            cap.drain(fluid, IFluidHandler.FluidAction.EXECUTE);
            break;
        }


        level.playLocalSound(context.getClickedPos(),
                SoundEvents.HONEY_DRINK, SoundSource.PLAYERS,0.5f, 0.5f, false);

        return InteractionResult.SUCCESS;
    }
}
