package io.github.hadron13.rubberworks.blocks.sapper;

import com.simibubi.create.AllFluids;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import io.github.hadron13.rubberworks.Rubberworks;
import io.github.hadron13.rubberworks.RubberworksLang;
import io.github.hadron13.rubberworks.register.RubberworksBlockEntities;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class SapperBlockEntity extends KineticBlockEntity implements IHaveHoveringInformation {

    public static final int NUM_LEAVES = 5;
    public static final int EXTENSION_TIME = 10;

    private int extendedTicks = 0;
    private float sapTimer = 0;
    public int sapperState = 0;
    private boolean valid = false;
    private boolean cached = false;
    //registers a few leaves to keep track of
    public BlockPos[] leafPos = new BlockPos[NUM_LEAVES];
    public SmartFluidTankBehaviour tank;

    private FluidStack outputFluid;

    public static final int RETRACTED = 1;
    public static final int EXTENDING = 2;
    public static final int EXTENDED = 3;
    public static final int RETRACTING = 4;

    public SapperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        extendedTicks = 0;
        sapTimer = 0f;
        sapperState = RETRACTED;
    }



    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RubberworksBlockEntities.SAPPER.get(),
                (be, context) -> {
                    if (context == null || SapperBlock.hasPipeTowards(be.getLevel(), be.getBlockPos(), be.getBlockState(), context)){
                        return be.tank.getCapability();
                    }
                    return null;
                }
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        float speed = Math.abs(getSpeed());

        checkValidity();

        boolean onClient = level.isClientSide && !isVirtual();

        switch (sapperState) {
            case RETRACTED -> {
                if (valid && speed > 0 && !isTankFull()) {
                    setSapperState(EXTENDING);


                    sapTimer = 200*32;

                }
            }
            case EXTENDING -> {
                extendedTicks++;
                if (extendedTicks == EXTENSION_TIME)
                    setSapperState(EXTENDED);

            }
            case EXTENDED -> {
                sapTimer -= (speed - (0.0019f * speed * speed));
                if(!valid || isTankFull()){
                    setSapperState(RETRACTING);
                    break;
                }

                if (sapTimer <= 0) {
                    if(onClient){
                        BlockPos pos = getBlockPos();
                        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.HONEY_DRINK, SoundSource.AMBIENT, 0.5f, 1.0f, false);

                        return;
                    }

                    SmartFluidTank localTank = tank.getPrimaryHandler();
                    if(outputFluid != null)
                        localTank.fill(outputFluid, EXECUTE);

                    sapTimer = 200*32;
                    sendData();
                }
            }
            case RETRACTING -> {
                extendedTicks--;
                if (extendedTicks == 0)
                    setSapperState(RETRACTED);
            }
        }

    }
    public void setSapperState(int newState){
        if(level != null && level.isClientSide && !isVirtual())
            return;
        sapperState = newState;
        sendData();
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
    }
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean kineticTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        boolean fluidTooltip = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        if(isTankFull())
            RubberworksLang.addHint(tooltip,"hint.sapper.full");

        return kineticTooltip || fluidTooltip;
    }
    public boolean isTankFull(){
        return tank.getPrimaryHandler().getFluidAmount() == 1000;
    }

    public void checkValidity(){
        if (level == null || !level.isLoaded(worldPosition) || level.isClientSide)
            return;
        BlockPos baseTrunkPos = getBlockPos().relative(getBlockState().getValue(HORIZONTAL_FACING).getOpposite(), 2);

        BlockState trunkType = level.getBlockState(baseTrunkPos);
        if(!TreeType.isValidLog(trunkType)) {
            cached = false;
            valid = false;
            return;
        }

        BlockPos trunkPos = baseTrunkPos.above();
        int treeHeight = 1;
        while (level.getBlockState(trunkPos) == trunkType) {
            trunkPos = trunkPos.above();
            treeHeight++;
        }
//        Rubberworks.LOGGER.debug("tree height: "+String.valueOf(treeHeight));
        if(treeHeight == 1){
            valid = false;
            cached = false;
            return;
        }

        int vertical = Math.min(level.getMaxBuildHeight() - worldPosition.getY(), treeHeight + 3);

        Iterable<BlockPos> leafArea = BlockPos.betweenClosed(   baseTrunkPos.offset(3,0,3),
                                                                baseTrunkPos.offset( -3, vertical, -3));

        if(cached){
            for (BlockPos pos: leafPos) {
                if (!level.getBlockState(pos).is(BlockTags.LEAVES)) {
                    cached = false;
                    valid = false;

                    break;
                }
            }
        }
        if(cached) return;

        int leafCount = 0;
        Block leafType = null;

        for(BlockPos pos : leafArea){
            BlockState leafState = level.getBlockState(pos);

            if(leafState.isAir())
                continue;

            if(leafType == null && TreeType.isValidTree(trunkType, leafState) ) {
                leafType = leafState.getBlock();
            }

            if(leafState.getBlock() == leafType){
                leafPos[leafCount] = pos.immutable();
                leafCount++;
                if(leafCount == NUM_LEAVES) {
                    cached = true;
                    valid = true;
//                    outputFluid = new FluidStack(AllFluids.CHOCOLATE.get(), 100);
                    outputFluid = TreeType.getFluid(trunkType.getBlock(), leafType);
                    Rubberworks.LOGGER.debug(outputFluid.toString());
                    return;
                }
            }
        }

    }


    public float getRenderedHeadOffset(float partialTicks) {

        final float startPos = 12 / 16f;
        final float finalPos = startPos + 6 / 16f;

        switch (sapperState){
            case RETRACTED  -> {return startPos;}
            case EXTENDED   -> {return finalPos;}
            case RETRACTING -> {partialTicks *= -1;}
        }
        float offset = (extendedTicks + partialTicks)/10f;

        float t =  1 - Mth.cos( offset * Mth.PI );
        t /= 2;

        return Mth.lerp(t, startPos, finalPos) ;
    }

    public float getRenderedHeadRotationSpeed(float partialTicks) {
        if (sapperState == EXTENDED) {
            return speed/ 2f;
        }
        return speed / 4f;
    }

    public void ponderFill(int ammount){
        SmartFluidTank localTank = tank.getPrimaryHandler();
        localTank.fill(new FluidStack(AllFluids.CHOCOLATE.get(), ammount),
                EXECUTE);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        super.tickAudio();
    }

    public static FluidStack getFluid(Block leaf, Block trunk){
        return new FluidStack(AllFluids.CHOCOLATE.get(), 150);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {

        compound.putFloat("sapTime", sapTimer);
        compound.putInt("sapState", sapperState);
        compound.putInt("exTime", extendedTicks);
        compound.putBoolean("val", valid);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        sapTimer = compound.getFloat("sapTime");
        sapperState = compound.getInt("sapState");
        extendedTicks = compound.getInt("exTime");
        valid = compound.getBoolean("val");
        super.read(compound, registries, clientPacket);
    }

    public static class TreeType{
        public static List<Block> logTypes = new ArrayList<>();
        public static HashMap<Couple<Block>, FluidStack> treeFluids = new HashMap<>();
        public static void registerTree(Block log, Block leaf, FluidStack fluid){
            logTypes.add(log);
            treeFluids.put(Couple.create(log, leaf), fluid);
        }
        public static boolean isValidLog(BlockState state){
            return logTypes.contains( state.getBlock() );
        }
        public static boolean isValidTree(BlockState log, BlockState leaf){
            return treeFluids.containsKey(Couple.create(log.getBlock(), leaf.getBlock()));
        }
        public static FluidStack getFluid(Block log, Block leaf){
            return treeFluids.get(Couple.create(log, leaf));
        }
    }
}